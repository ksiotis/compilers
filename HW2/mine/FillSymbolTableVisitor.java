import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.LinkedHashMap;
import java.util.Map;

public class FillSymbolTableVisitor extends GJDepthFirst<String, SymbolTable>{

    /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    @Override
    public String visit(Goal n, SymbolTable symbols) {
        n.f0.accept(this, symbols);
        n.f1.accept(this, symbols);

        return "File Parsed Successfully";
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
    @Override
    public String visit(MainClass n, SymbolTable symbols) {
        // create class with name (f1)
        String className = n.f1.accept(this, symbols);
        ClassTable myClass = new ClassTable(className);

        // create main method in class (f6) returning void (f5)
        FunctionTable myfunc = new FunctionTable("main", "void");

        // set arg of main to String[] (f8-f10) with name (f11)
        String argName = n.f11.accept(this, symbols);
        myfunc.args.put(argName, "String[]");
        myfunc.vars = new String[1]; //can have 1 argument

        //add main method to class
        myClass.methods.put("main", myfunc);

        // add class to SymbolTable
        if (symbols.classes.putIfAbsent(className, myClass) != null) {
            throw new RuntimeException("Duplicate method main(String[]) in type "+className);
        }

        //set currentClass in symbolTable
        symbols.currentClass = myClass;

        /*TODO
         get variables (f14)

         exec statements (f15)
        */

        return null;
    }

    /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    @Override
    public String visit(TypeDeclaration n, SymbolTable symbols) {
        n.f0.accept(this);
    }

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
    @Override
    public String visit(ClassDeclaration n, SymbolTable symbols)) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
    }

    // /**
    // * f0 -> "class"
    // * f1 -> Identifier()
    // * f2 -> "extends"
    // * f3 -> Identifier()
    // * f4 -> "{"
    // * f5 -> ( VarDeclaration() )*
    // * f6 -> ( MethodDeclaration() )*
    // * f7 -> "}"
    // */
    // @Override
    // public void visit(ClassExtendsDeclaration n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    //     n.f3.accept(this);
    //     n.f4.accept(this);
    //     n.f5.accept(this);
    //     n.f6.accept(this);
    //     n.f7.accept(this);
    // }

    // /**
    // * f0 -> Type()
    // * f1 -> Identifier()
    // * f2 -> ";"
    // */
    // @Override
    // public void visit(VarDeclaration n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    // }

    // /**
    // * f0 -> "public"
    // * f1 -> Type()
    // * f2 -> Identifier()
    // * f3 -> "("
    // * f4 -> ( FormalParameterList() )?
    // * f5 -> ")"
    // * f6 -> "{"
    // * f7 -> ( VarDeclaration() )*
    // * f8 -> ( Statement() )*
    // * f9 -> "return"
    // * f10 -> Expression()
    // * f11 -> ";"
    // * f12 -> "}"
    // */
    // @Override
    // public void visit(MethodDeclaration n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    //     n.f3.accept(this);
    //     n.f4.accept(this);
    //     n.f5.accept(this);
    //     n.f6.accept(this);
    //     n.f7.accept(this);
    //     n.f8.accept(this);
    //     n.f9.accept(this);
    //     n.f10.accept(this);
    //     n.f11.accept(this);
    //     n.f12.accept(this);
    // }

    // /**
    // * f0 -> FormalParameter()
    // * f1 -> FormalParameterTail()
    // */
    // @Override
    // public void visit(FormalParameterList n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    // }

    // /**
    // * f0 -> Type()
    // * f1 -> Identifier()
    // */
    // @Override
    // public void visit(FormalParameter n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    // }

    // /**
    // * f0 -> ( FormalParameterTerm() )*
    // */
    // @Override
    // public void visit(FormalParameterTail n) {
    //     n.f0.accept(this);
    // }

    // /**
    // * f0 -> ","
    // * f1 -> FormalParameter()
    // */
    // @Override
    // public void visit(FormalParameterTerm n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    // }

    // /**
    // * f0 -> ArrayType()
    // *       | BooleanType()
    // *       | IntegerType()
    // *       | Identifier()
    // */
    // @Override
    // public void visit(Type n) {
    //     n.f0.accept(this);
    // }

    // /**
    // * f0 -> "int"
    // * f1 -> "["
    // * f2 -> "]"
    // */
    // @Override
    // public void visit(ArrayType n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    // }

    // /**
    // * f0 -> "boolean"
    // */
    // @Override
    // public void visit(BooleanType n) {
    //     n.f0.accept(this);
    // }

    // /**
    // * f0 -> "int"
    // */
    // @Override
    // public void visit(IntegerType n) {
    //     n.f0.accept(this);
    // }

    // /**
    // * f0 -> Block()
    // *       | AssignmentStatement()
    // *       | ArrayAssignmentStatement()
    // *       | IfStatement()
    // *       | WhileStatement()
    // *       | PrintStatement()
    // */
    // @Override
    // public void visit(Statement n) {
    //     n.f0.accept(this);
    // }

    // /**
    // * f0 -> "{"
    // * f1 -> ( Statement() )*
    // * f2 -> "}"
    // */
    // @Override
    // public void visit(Block n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    // }

    // /**
    // * f0 -> Identifier()
    // * f1 -> "="
    // * f2 -> Expression()
    // * f3 -> ";"
    // */
    // @Override
    // public void visit(AssignmentStatement n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    //     n.f3.accept(this);
    // }

    // /**
    // * f0 -> Identifier()
    // * f1 -> "["
    // * f2 -> Expression()
    // * f3 -> "]"
    // * f4 -> "="
    // * f5 -> Expression()
    // * f6 -> ";"
    // */
    // @Override
    // public void visit(ArrayAssignmentStatement n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    //     n.f3.accept(this);
    //     n.f4.accept(this);
    //     n.f5.accept(this);
    //     n.f6.accept(this);
    // }

    // /**
    // * f0 -> "if"
    // * f1 -> "("
    // * f2 -> Expression()
    // * f3 -> ")"
    // * f4 -> Statement()
    // * f5 -> "else"
    // * f6 -> Statement()
    // */
    // @Override
    // public void visit(IfStatement n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    //     n.f3.accept(this);
    //     n.f4.accept(this);
    //     n.f5.accept(this);
    //     n.f6.accept(this);
    // }

    // /**
    // * f0 -> "while"
    // * f1 -> "("
    // * f2 -> Expression()
    // * f3 -> ")"
    // * f4 -> Statement()
    // */
    // @Override
    // public void visit(WhileStatement n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    //     n.f3.accept(this);
    //     n.f4.accept(this);
    // }

    // /**
    // * f0 -> "System.out.println"
    // * f1 -> "("
    // * f2 -> Expression()
    // * f3 -> ")"
    // * f4 -> ";"
    // */
    // @Override
    // public void visit(PrintStatement n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    //     n.f3.accept(this);
    //     n.f4.accept(this);
    // }

    // /**
    // * f0 -> AndExpression()
    // *       | CompareExpression()
    // *       | PlusExpression()
    // *       | MinusExpression()
    // *       | TimesExpression()
    // *       | ArrayLookup()
    // *       | ArrayLength()
    // *       | MessageSend()
    // *       | Clause()
    // */
    // @Override
    // public void visit(Expression n) {
    //     n.f0.accept(this);
    // }

    // /**
    // * f0 -> Clause()
    // * f1 -> "&&"
    // * f2 -> Clause()
    // */
    // @Override
    // public void visit(AndExpression n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    // }

    // /**
    // * f0 -> PrimaryExpression()
    // * f1 -> "<"
    // * f2 -> PrimaryExpression()
    // */
    // @Override
    // public void visit(CompareExpression n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    // }

    // /**
    // * f0 -> PrimaryExpression()
    // * f1 -> "+"
    // * f2 -> PrimaryExpression()
    // */
    // @Override
    // public void visit(PlusExpression n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    // }

    // /**
    // * f0 -> PrimaryExpression()
    // * f1 -> "-"
    // * f2 -> PrimaryExpression()
    // */
    // @Override
    // public void visit(MinusExpression n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    // }

    // /**
    // * f0 -> PrimaryExpression()
    // * f1 -> "*"
    // * f2 -> PrimaryExpression()
    // */
    // @Override
    // public void visit(TimesExpression n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    // }

    // /**
    // * f0 -> PrimaryExpression()
    // * f1 -> "["
    // * f2 -> PrimaryExpression()
    // * f3 -> "]"
    // */
    // @Override
    // public void visit(ArrayLookup n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    //     n.f3.accept(this);
    // }

    // /**
    // * f0 -> PrimaryExpression()
    // * f1 -> "."
    // * f2 -> "length"
    // */
    // @Override
    // public void visit(ArrayLength n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    // }

    // /**
    // * f0 -> PrimaryExpression()
    // * f1 -> "."
    // * f2 -> Identifier()
    // * f3 -> "("
    // * f4 -> ( ExpressionList() )?
    // * f5 -> ")"
    // */
    // @Override
    // public void visit(MessageSend n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    //     n.f3.accept(this);
    //     n.f4.accept(this);
    //     n.f5.accept(this);
    // }

    // /**
    // * f0 -> Expression()
    // * f1 -> ExpressionTail()
    // */
    // @Override
    // public void visit(ExpressionList n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    // }

    // /**
    // * f0 -> ( ExpressionTerm() )*
    // */
    // @Override
    // public void visit(ExpressionTail n) {
    //     n.f0.accept(this);
    // }

    // /**
    // * f0 -> ","
    // * f1 -> Expression()
    // */
    // @Override
    // public void visit(ExpressionTerm n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    // }

    // /**
    // * f0 -> NotExpression()
    // *       | PrimaryExpression()
    // */
    // @Override
    // public void visit(Clause n) {
    //     n.f0.accept(this);
    // }

    // /**
    // * f0 -> IntegerLiteral()
    // *       | TrueLiteral()
    // *       | FalseLiteral()
    // *       | Identifier()
    // *       | ThisExpression()
    // *       | ArrayAllocationExpression()
    // *       | AllocationExpression()
    // *       | BracketExpression()
    // */
    // @Override
    // public void visit(PrimaryExpression n) {
    //     n.f0.accept(this);
    // }

    // /**
    // * f0 -> <INTEGER_LITERAL>
    // */
    // @Override
    // public void visit(IntegerLiteral n) {
    //     n.f0.accept(this);
    // }

    // /**
    // * f0 -> "true"
    // */
    // @Override
    // public void visit(TrueLiteral n) {
    //     n.f0.accept(this);
    // }

    // /**
    // * f0 -> "false"
    // */
    // @Override
    // public void visit(FalseLiteral n) {
    //     n.f0.accept(this);
    // }

    /**
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, SymbolTable symbols) {
        return n.f0.accept(this, symbols);
    }

    // /**
    // * f0 -> "this"
    // */
    // @Override
    // public void visit(ThisExpression n) {
    //     n.f0.accept(this);
    // }

    // /**
    // * f0 -> "new"
    // * f1 -> "int"
    // * f2 -> "["
    // * f3 -> Expression()
    // * f4 -> "]"
    // */
    // @Override
    // public void visit(ArrayAllocationExpression n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    //     n.f3.accept(this);
    //     n.f4.accept(this);
    // }

    // /**
    // * f0 -> "new"
    // * f1 -> Identifier()
    // * f2 -> "("
    // * f3 -> ")"
    // */
    // @Override
    // public void visit(AllocationExpression n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    //     n.f3.accept(this);
    // }

    // /**
    // * f0 -> "!"
    // * f1 -> Clause()
    // */
    // @Override
    // public void visit(NotExpression n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    // }

    // /**
    // * f0 -> "("
    // * f1 -> Expression()
    // * f2 -> ")"
    // */
    // @Override
    // public void visit(BracketExpression n) {
    //     n.f0.accept(this);
    //     n.f1.accept(this);
    //     n.f2.accept(this);
    // }



}
