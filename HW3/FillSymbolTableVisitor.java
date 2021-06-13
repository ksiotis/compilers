import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;

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
    @Override
    public String visit(MainClass n, SymbolTable symbols) {

        // create class with name (f1)
        String className = n.f1.accept(this, symbols);
        ClassTable myClass = new ClassTable(className);

        // create main method in class (f6) returning void (f5)
        FunctionTable myfunc = new FunctionTable(myClass, "main", "void");
        // set arg of main to String[] (f8-f10) with name (f11)
        String argName = n.f11.accept(this, symbols);
        myfunc.args.put(argName, "String[]");

        //add main method to class
        myClass.methods.put("main", myfunc);
        // add class to SymbolTable
        if (symbols.classes.putIfAbsent(className, myClass) != null) {
            throw new RuntimeException("Duplicate method main(String[]) in type "+className);
        }

        //set currentClass in symbolTable
        symbols.currentClass = myClass;
        symbols.currentFunction = myClass.methods.get("main");
        // add variables
        n.f14.accept(this, symbols);

        //exit main
        symbols.currentClass = null;
        symbols.currentFunction = null;

        return null;
    }

    /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    @Override
    public String visit(TypeDeclaration n, SymbolTable symbols) {
        n.f0.accept(this, symbols);
        return null;
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
    public String visit(ClassDeclaration n, SymbolTable symbols) {
        
        String className = n.f1.accept(this, symbols);
        ClassTable myClass = new ClassTable(className);
        if (symbols.classes.putIfAbsent(className, myClass) != null) {
            throw new RuntimeException("The type "+className+" is already defined");
        }

        // set current class in symbolTable
        symbols.currentClass = myClass;

        n.f3.accept(this, symbols);

        n.f4.accept(this, symbols);

        // exit class
        symbols.currentClass = null;
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
    @Override
    public String visit(ClassExtendsDeclaration n, SymbolTable symbols) {
        String className = n.f1.accept(this, symbols);
        String parent = n.f3.accept(this, symbols);

        //make sure parent class exists
        ClassTable parentClass = symbols.classes.get(parent);
        if (parentClass == null) {
            throw new RuntimeException(parent+" cannot be resolved to a type");
        }

        //make sure current class doesn't already exist
        ClassTable myClass = new ClassTable(className, parentClass);
        if (symbols.classes.putIfAbsent(className, myClass) != null) {
            throw new RuntimeException("The type "+className+" is already defined");
        }

        // set current class in symbolTable
        symbols.currentClass = myClass;

        n.f5.accept(this, symbols);

        n.f6.accept(this, symbols);

        symbols.currentClass = null; //exit current class

        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public String visit(VarDeclaration n, SymbolTable symbols) {
        
        String type = n.f0.accept(this, symbols);
        String name = n.f1.accept(this, symbols);
        
        LinkedHashMap<String, String> target;

        //if this is a local variable
        if (symbols.currentFunction != null) {
            target = symbols.currentFunction.localVars;

            // if it already exists in arguments
            if (symbols.currentFunction.args.get(name) != null) {
                throw new RuntimeException("Duplicate local variable "+name+" to argument");
            }
            //check if double decleration
            if (target.putIfAbsent(name, type) != null) {
                throw new RuntimeException("Duplicate local variable "+name);
            }
        }
        //if this is a class field
        else {
            target = symbols.currentClass.fields;
            if (target.putIfAbsent(name, type) != null) {
                throw new RuntimeException("Duplicate field "+symbols.currentClass.name+"."+name);
            }
        }

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
    @Override
    public String visit(MethodDeclaration n, SymbolTable symbols) {
        //get name and type
        String type = n.f1.accept(this, symbols);
        String name = n.f2.accept(this, symbols);

        //create method
        FunctionTable myMethod = new FunctionTable(symbols.currentClass, name, type);
        symbols.currentFunction = myMethod;
        
        //handle arguments
        if (n.f4.present())
            n.f4.accept(this, symbols);
        
        FunctionTable t = symbols.currentClass.hasMethod(name);

        //try to add it
        if (symbols.currentClass.methods.putIfAbsent(name, myMethod) != null) {
            throw new RuntimeException("Duplicate method "+myMethod.toString()+" in type "+symbols.currentClass.name);
        }
        //check if method is being overloaded
        if (t != null) {
            if (!linkedHashMapEquals(t.args, myMethod.args) || t.type != myMethod.type) {
                throw new RuntimeException("Overloaded method "+myMethod.toString()+" in type "+symbols.currentClass.name+" with "+t.toString());
            }
        }

        //variables decleration
        n.f7.accept(this, symbols);

        //exiting current method
        symbols.currentFunction = null;
        return null;
    }

    /**
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */
    @Override
    public String visit(FormalParameterList n, SymbolTable symbols) {
        n.f0.accept(this, symbols);
        n.f1.accept(this, symbols);

        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
    @Override
    public String visit(FormalParameter n, SymbolTable symbols) {
        String type = n.f0.accept(this, symbols);
        String name = n.f1.accept(this, symbols);

        if (symbols.currentFunction.args.putIfAbsent(name, type) != null) {
            throw new RuntimeException("Duplicate parameter "+ name);
        }

        return null;
    }

    /**
    * f0 -> ( FormalParameterTerm() )*
    */
    @Override
    public String visit(FormalParameterTail n, SymbolTable symbols) {
        if (n.f0.present())
            n.f0.accept(this, symbols);
        return null;
    }

    /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
    @Override
    public String visit(FormalParameterTerm n, SymbolTable symbols) {
        n.f1.accept(this, symbols);
        return null;
    }

    /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    @Override
    public String visit(Type n, SymbolTable symbols) {
        return n.f0.accept(this, symbols);
    }

    /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
    @Override
    public String visit(ArrayType n, SymbolTable symbols) {
        return "int[]";
    }

    /**
    * f0 -> "boolean"
    */
    @Override
    public String visit(BooleanType n, SymbolTable symbols) {
        return "boolean";
    }


    /**
    * f0 -> "int"
    */
    @Override
    public String visit(IntegerType n, SymbolTable symbols) {
        return "int";
    }

    /**
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, SymbolTable symbols) {
        return n.f0.toString();
    }

    public static boolean linkedHashMapEquals(Map<String, String> leftMap, Map<String, String> rightMap) {
        Iterator<Map.Entry<String, String>> lIter = leftMap.entrySet().iterator();
        Iterator<Map.Entry<String, String>> rIter = rightMap.entrySet().iterator();
        while (lIter.hasNext() && rIter.hasNext()) {
            Map.Entry<String, String> left = lIter.next();
            Map.Entry<String, String> right = rIter.next();
            if (left.getValue() != right.getValue()) {
                return false;
            }
        }
        return !lIter.hasNext() && !rIter.hasNext();
    }
}
