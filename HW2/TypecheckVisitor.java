import syntaxtree.*;
import visitor.GJDepthFirst;
// import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class TypecheckVisitor extends GJDepthFirst<String, SymbolTable>{

    /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    public String visit(Goal n, SymbolTable symbols) {
        n.f0.accept(this, symbols);
        n.f1.accept(this, symbols);
        n.f2.accept(this, symbols);
        return null;
    }

    /**
     * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
    public String visit(MainClass n, SymbolTable symbols) {

        String currClass = n.f1.accept(this, symbols);

        // entering main
        symbols.currentClass = symbols.classes.get(currClass);
        symbols.currentFunction = symbols.currentClass.methods.get("main");

        //check statements
        n.f15.accept(this, symbols);

        // exit main
        symbols.currentClass = null;
        return null;
    }

    /**
     * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    public String visit(TypeDeclaration n, SymbolTable argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
    public String visit(ClassDeclaration n, SymbolTable symbols) {
        symbols.currentClass = symbols.classes.get(n.f1.accept(this, symbols));

        n.f4.accept(this, symbols);
        return null;
    }

    /**
     * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
    public String visit(ClassExtendsDeclaration n, SymbolTable symbols) {
        symbols.currentClass = symbols.classes.get(n.f1.accept(this, symbols));

        n.f6.accept(this, symbols);

        symbols.currentClass = null;
        return null;
    }

    /**
     * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
    public String visit(MethodDeclaration n, SymbolTable symbols) {
        String name = n.f2.accept(this, symbols);
        
        FunctionTable myMethod = symbols.currentClass.methods.get(name);
        symbols.currentFunction = myMethod;
        
        String exptype = symbols.currentFunction.type;

        n.f8.accept(this, symbols);

        String type = n.f10.accept(this, symbols);
        // !isSubclassOf(current, entry.getValue(), symbols)
        if (!isSubclassOf(type,exptype,symbols)) {
            throw new RuntimeException("Type mismatch: expected type "+exptype+" received type "+type);
        }

        symbols.currentFunction = null;
        return null;
    }

    /**
     * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
    public String visit(Statement n, SymbolTable argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
    public String visit(Block n, SymbolTable argu) {

        n.f1.accept(this, argu);

        return null;
    }

    /**
     * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    public String visit(AssignmentStatement n, SymbolTable symbols) {

        String name = n.f0.accept(this, symbols);
        String expectedType = symbols.currentFunction.canAccessVariable(name);

        //if variable does not exist throw error
        if (expectedType == null) {
            throw new RuntimeException(name+" cannot be resolved to a variable");
        }

        //get returned type
        String actualType = n.f2.accept(this, symbols);
        if (actualType == "this") {
            actualType = symbols.currentFunction.parent.name;
        }
        if (!isSubclassOf(actualType,expectedType,symbols)) {
            throw new RuntimeException("Type mismatch: expected type "+expectedType+" received type "+actualType);
        }

        return null;
    }

    /**
     * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
    public String visit(ArrayAssignmentStatement n, SymbolTable symbols) {
        String name = n.f0.accept(this, symbols);
        String num = n.f2.accept(this, symbols);
        String val = n.f5.accept(this, symbols);

        String type = symbols.currentFunction.canAccessVariable(name);
        //if iden is identifier temp will have a value
        if (type == null) {
            throw new RuntimeException(name+" cannot be resolved to a variable");
        }
        
        //iden is type int[]?
        if (type != "int[]") {
            throw new RuntimeException("Type mismatch: expected type int[] received type "+type);
        }
        
        //num is type int?
        if (num != "int") {
            throw new RuntimeException("Type mismatch: expected type int received type "+num);
        }

        //val is type int?
        if (val != "int") {
            throw new RuntimeException("Type mismatch: expected type int received type "+val);
        }

        return null;
    }

    /**
     * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    public String visit(IfStatement n, SymbolTable argu) {
        String exp = n.f2.accept(this, argu);
        if (exp != "boolean") {
            throw new RuntimeException("Type mismatch: expected type boolean received type "+exp);
        }
        n.f4.accept(this, argu);
        n.f6.accept(this, argu);

        return null;
    }

    /**
     * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    public String visit(WhileStatement n, SymbolTable argu) {
        String exp = n.f2.accept(this, argu);
        if (exp != "boolean") {
            throw new RuntimeException("Type mismatch: expected type boolean received type "+exp);
        }
        n.f4.accept(this, argu);
        return null;
    }

    /**
     * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    public String visit(PrintStatement n, SymbolTable argu) {
        String type = n.f2.accept(this, argu);
        if (type != "int") {
            throw new RuntimeException("Type mismatch: expected type int received type "+type);
        }
        return null;
    }

    /**
     * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | Clause()
    */
    public String visit(Expression n, SymbolTable symbols) {
        return n.f0.accept(this, symbols);
    }

    /**
     * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    public String visit(AndExpression n, SymbolTable argu) {
        String cl1 = n.f0.accept(this, argu);
        String cl2 = n.f2.accept(this, argu);

        if (cl1 != "boolean") {
            throw new RuntimeException("The operator && is undefined for the argument type "+cl1);
        }
        if (cl2 != "boolean") {
            throw new RuntimeException("The operator && is undefined for the argument type "+cl2);
        }
        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n, SymbolTable argu) {
        String exp1 = n.f0.accept(this, argu);
        String exp2 = n.f2.accept(this, argu);

        if (exp1 != "int") {
            throw new RuntimeException("The operator < is undefined for the argument type "+exp1);
        }
        if (exp2 != "int") {
            throw new RuntimeException("The operator < is undefined for the argument type "+exp2);
        }
        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    public String visit(PlusExpression n, SymbolTable argu) {
        String exp1 = n.f0.accept(this, argu);
        String exp2 = n.f2.accept(this, argu);

        if (exp1 != "int") {
            throw new RuntimeException("The operator + is undefined for the argument type "+exp1);
        }
        if (exp2 != "int") {
            throw new RuntimeException("The operator + is undefined for the argument type "+exp2);
        }
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    public String visit(MinusExpression n, SymbolTable argu) {
        String exp1 = n.f0.accept(this, argu);
        String exp2 = n.f2.accept(this, argu);

        if (exp1 != "int") {
            throw new RuntimeException("The operator - is undefined for the argument type "+exp1);
        }
        if (exp2 != "int") {
            throw new RuntimeException("The operator - is undefined for the argument type "+exp2);
        }
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    public String visit(TimesExpression n, SymbolTable argu) {
        String exp1 = n.f0.accept(this, argu);
        String exp2 = n.f2.accept(this, argu);

        if (exp1 != "int") {
            throw new RuntimeException("The operator * is undefined for the argument type "+exp1);
        }
        if (exp2 != "int") {
            throw new RuntimeException("The operator * is undefined for the argument type "+exp2);
        }
        return "int";
    }
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, SymbolTable argu) {
        String exp1 = n.f0.accept(this, argu);
        String exp2 = n.f2.accept(this, argu);
        
        if (exp1 != "int[]") {
            throw new RuntimeException("The type of the expression must be int[] type but it resolved to "+exp1);
        }
        if (exp2 != "int") {
            throw new RuntimeException("Type mismatch: expected type int received type "+exp2);
        }
        
        return "int";
    }
    
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, SymbolTable argu) {
        String exp1 = n.f0.accept(this, argu);
        if (exp1 != "int[]") {
            throw new RuntimeException("The type of the expression must be int[] type but it resolved to "+exp1);
        }
        
        return "int";
    }

    /**
     * f0 -> PrimaryExpression() 
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    public String visit(MessageSend n, SymbolTable symbols) {
        ClassTable old = symbols.currentClass;
        FunctionTable old2 = symbols.currentFunction;

        String myclass = n.f0.accept(this, symbols);
        String fieldOrMethod = n.f2.accept(this, symbols);

        String ret = null;

        //check if myclass is actually a class and enter it
        if (myclass == "this") {
            symbols.currentClass = old;
        }
        else {
            symbols.currentClass = symbols.classes.get(myclass);
        }
        if (symbols.currentClass == null) {
            throw new RuntimeException(myclass+" cannot be resolved to a variable");
        }

        //check if fieldOrMethod is a field or a method in target class
        String myfield = symbols.currentClass.hasField(fieldOrMethod);
        FunctionTable mymethod = symbols.currentClass.hasMethod(fieldOrMethod);

        // if it is a field
        if (myfield != null && !n.f4.present() && mymethod == null){
            ret = myfield; //return its type
        }

        // if it is a method
        if (mymethod != null) {
            if (symbols.currentFunction.name=="bar" && mymethod.name == "foo" && symbols.currentClass.name == "A") {
                int a = 0;//TODO remove
            }

            //handle arguments of fieldOrMethod in ExpressionList
            List<String> tempVars = new ArrayList<String>(symbols.vars);
            symbols.vars.clear();
            n.f4.accept(this, symbols);
            

            //check if ExpressionList gave the correct arguments
            for (Map.Entry<String,String> entry : mymethod.args.entrySet()) {
                String current = symbols.vars.remove(0);
                if (current == "this") {
                    current = old.name;
                }
                if (!isSubclassOf(current, entry.getValue(), symbols)) { //if a given argument is not correct
                    throw new RuntimeException("The method "+mymethod.toString()+" in the type "+symbols.currentClass.name+" is not applicable for the argument type "+current+" instead of "+entry.getValue());
                }
            }
            if (symbols.vars.size() > 0) { //if more arguments were given
                throw new RuntimeException("The method "+mymethod.toString()+" in the type "+symbols.currentClass.name+" is not applicable for these argument types");
            }

            symbols.vars = tempVars;
            ret = mymethod.type;
        }

        symbols.currentClass = old;
        symbols.currentFunction = old2;

        return ret;
    }

    /**
     * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    public String visit(ExpressionList n, SymbolTable symbols) {

        String name = n.f0.accept(this, symbols);
        String type = symbols.currentFunction.canAccessVariable(name);

        symbols.vars.add(type != null ? type : name);
        n.f1.accept(this, symbols);

        return null;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
    */
    public String visit(ExpressionTail n, SymbolTable symbols) {
        n.f0.accept(this, symbols);
        return null;
    }

    /**
     * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n, SymbolTable symbols) {
        String name = n.f1.accept(this, symbols);
        String type = symbols.currentFunction.canAccessVariable(name);
        
        symbols.vars.add(type != null ? type : name);

        return null;
    }

    /**
     * f0 -> NotExpression()
    *       | PrimaryExpression()
    */
    public String visit(Clause n, SymbolTable symbols) {
        String name = n.f0.accept(this, symbols);
        String type = symbols.currentFunction.canAccessVariable(name);
        if (type != null) {
            return type;
        }
        else {
            return name;
        }
    }

    /**
     * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | BracketExpression()
    */
    public String visit(PrimaryExpression n, SymbolTable symbols) {
        String ret = n.f0.accept(this, symbols);

        String temp;
        if (symbols.currentFunction != null) {
            temp = symbols.currentFunction.canAccessVariable(ret);
        }
        else {
            temp = symbols.currentClass.hasField(ret);
        }
        //if ret is identifier temp will have a value
        if (temp != null) {
            ret = temp;
        }

        return ret;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n, SymbolTable argu) {
        return "int";
    }

    /**
     * f0 -> "true"
    */
    public String visit(TrueLiteral n, SymbolTable argu) {
        return "boolean";
    }

    /**
     * f0 -> "false"
    */
    public String visit(FalseLiteral n, SymbolTable argu) {
        return "boolean";
    }

    /**
     * f0 -> <IDENTIFIER>
    */
    public String visit(Identifier n, SymbolTable argu) {
        return n.f0.toString();
    }

    /**
     * f0 -> "this"
    */
    public String visit(ThisExpression n, SymbolTable symbols) {
        // return symbols.currentClass.name;
        return "this";
    }

    /**
     * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public String visit(ArrayAllocationExpression n, SymbolTable argu) {
        
        String exp = n.f3.accept(this, argu);
        if (exp != "int") {
            throw new RuntimeException("Type mismatch: expected type int, returned type "+exp);
        }
        return "int[]";

    }

    /**
     * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    public String visit(AllocationExpression n, SymbolTable symbols) {
        String name = n.f1.accept(this, symbols);
        
        // check if class exists
        ClassTable myclass = symbols.classes.get(name);
        if (myclass == null) {
            throw new RuntimeException(name+" cannot be resolved to a type");
        }

        return myclass.name;
    }

    /**
     * f0 -> "!"
    * f1 -> Clause()
    */
    public String visit(NotExpression n, SymbolTable argu) {
        String clause = n.f1.accept(this, argu);
        if (clause != "boolean") {
            throw new RuntimeException("Type mismatch: expected type boolean, returned type "+clause);
        }
        return "boolean";
    }

    /**
     * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    public String visit(BracketExpression n, SymbolTable argu) {
        return n.f1.accept(this, argu);
    }

    boolean isSubclassOf(String child, String parent, SymbolTable symbols) {
        if (child == "this") {
            child = symbols.currentClass.name;
        }

        ClassTable c = symbols.classes.get(child);
        if (child == parent) {
            return true;
        }
        else if (c != null && c.parent != null) {
            return isSubclassOf(c.parent.name, parent, symbols);
        }

       return false;
   }

}
