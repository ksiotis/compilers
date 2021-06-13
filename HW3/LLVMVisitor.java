import syntaxtree.*;
import visitor.GJDepthFirst;
import java.io.BufferedWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.IOException;
import java.lang.reflect.Method;

public class LLVMVisitor extends GJDepthFirst<String, String> {
    SymbolTable symbols;
    ClassOffsetsContainer offsets;
    BufferedWriter file;
    Integer registerCounter = 0;
    Integer ifCounter = 0;
    Integer loopCounter = 0;
    String buffer = "";

    public void setOffsets(SymbolTable arg1, ClassOffsetsContainer arg2, BufferedWriter arg3) {
        this.symbols = arg1;
        this.offsets = arg2;
        this.file = arg3;
    }

    public void emit(BufferedWriter file, String str) {
        try {
            file.write(str);
        } catch (IOException e) {
            System.out.println(e);
            throw new RuntimeException();
        }
    }

    String emptyBuffer() {
        String ret = new String(this.buffer);
        this.buffer = "";

        return ret;
    }

    String LLtype(String type) {
        String ret;
        switch (type) {
            case "int":
                ret = "i32";
                break;
            case "int*":
                ret = "i32*";
                break;
            case "boolean":
                ret = "i1";
                break;
            case "boolean*":
                ret = "i1*";
                break;
            default:
                ret = "i8*";
                break;
        }
        return ret;
    }

    void writeStartingLLThings(BufferedWriter file) {
        for (Map.Entry<String, ClassTable> entry : this.symbols.classes.entrySet()) {
            String className = entry.getKey();
            Integer methodsNum = entry.getValue().methods.size();
            if (entry.getValue().methods.keySet().contains("main")) methodsNum -= 1;

            String strMethods = "";
            // for each method
            for (Map.Entry<String, FunctionTable> method : entry.getValue().methods.entrySet()) {
                // skip main
                if (method.getKey() == "main") continue;

                //get type
                String methodType = LLtype(method.getValue().type);
                String methodName = method.getKey();

                String strArgs = "i8*";
                for (String arg : method.getValue().args.values()) {
                    strArgs += "," + LLtype(arg);
                }
                
                strMethods += "i8* bitcast ("+methodType+" ("+strArgs+")* @"+
                                    className+"."+methodName+" to i8*), ";
            }
            //remove trailing ", "
            if (strMethods.length() > 0)
                strMethods = strMethods.substring(0, strMethods.length()-2);

            String line = "@."+className+"_vtable = global ["+methodsNum+" x i8*] ["+strMethods+"]\n";

            emit(file, line);

            emit(file,
                "declare i8* @calloc(i32, i32)\n"+
                "declare i32 @printf(i8*, ...)\n"+
                "declare void @exit(i32)\n"+
                "\n"+
                "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n"+
                "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n"+
                "@_cNSZ = constant [15 x i8] c\"Negative size\\0a\\00\"\n"+
                "\n"+
                "define void @print_int(i32 %i) {\n"+
                "\t%_str = bitcast [4 x i8]* @_cint to i8*\n"+
                "\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n"+
                "\tret void\n"+
                "}\n"+
                "\n"+
                "define void @throw_oob() {\n"+
                "\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n"+
                "\tcall i32 (i8*, ...) @printf(i8* %_str)\n"+
                "\tcall void @exit(i32 1)\n"+
                "\tret void\n"+
                "}\n"+
                "\n"+
                "define void @throw_nsz() {\n"+
                "\t%_str = bitcast [15 x i8]* @_cNSZ to i8*\n"+
                "\tcall i32 (i8*, ...) @printf(i8* %_str)\n"+
                "\tcall void @exit(i32 1)\n"+
                "\tret void\n"+
                "}\n\n"
            );
        }
    }

    String newRegister() {
        String register = "%_"+this.registerCounter;
        this.registerCounter++;
        return register;
    }

    void resetCounters() {
        this.registerCounter = 0;
        this.ifCounter = 0;
        this.loopCounter = 0;
    }

    String getMethodVarType(String name) {
        String type = symbols.currentFunction.localVars.get(name);
        if (type == null) {
            type = symbols.currentFunction.args.get(name);
        }
        // if (type == null) {
        //     type = symbols.currentClass.fields.get(name);
        // }

        return type;
    }

    Integer getOffset(String name) {
        Integer myOffset = -1;
        ClassOffsets myclass = offsets.offsets.get(symbols.currentClass.name);
        if (myclass != null) {
            myOffset = myclass.methodOffsets.get(symbols.currentFunction.name);
        }
        return (myOffset != null && myOffset >= 0) ? myOffset : -1;
    }


    /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    @Override
    public String visit(Goal n, String argu) {
        n.f0.accept(this, null);
        n.f1.accept(this, null);
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
    public String visit(MainClass n, String argu) {
        String classname = n.f1.accept(this, null);
        symbols.currentClass = symbols.classes.get(classname);
        symbols.currentFunction = symbols.currentClass.methods.get("main");

        emit(file, "define i32 @main() {\n");

        n.f14.accept(this, null);
        String declarations = emptyBuffer();
        emit(file, declarations);

        n.f15.accept(this, null);
        String statements = emptyBuffer();
        emit(file, statements);

        emit(file, "\n\tret i32 0\n}\n");

        symbols.currentClass = null;
        symbols.currentFunction = null;
        return null;
    }
  
   //   /**
   //    * f0 -> ClassDeclaration()
   //    *       | ClassExtendsDeclaration()
   //    */
   //   public void visit(TypeDeclaration n, String argu) {
   //      return n.f0.accept(this, file);
   //   }
  
   //   /**
   //    * f0 -> "class"
   //    * f1 -> Identifier()
   //    * f2 -> "{"
   //    * f3 -> ( VarDeclaration() )*
   //    * f4 -> ( MethodDeclaration() )*
   //    * f5 -> "}"
   //    */
   //   public void visit(ClassDeclaration n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      n.f3.accept(this, file);
   //      n.f4.accept(this, file);
   //      n.f5.accept(this, file);
   //      return _ret;
   //   }
  
   //   /**
   //    * f0 -> "class"
   //    * f1 -> Identifier()
   //    * f2 -> "extends"
   //    * f3 -> Identifier()
   //    * f4 -> "{"
   //    * f5 -> ( VarDeclaration() )*
   //    * f6 -> ( MethodDeclaration() )*
   //    * f7 -> "}"
   //    */
   //   public void visit(ClassExtendsDeclaration n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      n.f3.accept(this, file);
   //      n.f4.accept(this, file);
   //      n.f5.accept(this, file);
   //      n.f6.accept(this, file);
   //      n.f7.accept(this, file);
   //      return _ret;
   //   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public String visit(VarDeclaration n, String argu) {
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);
        
        buffer += "\t%"+name+" = alloca "+LLtype(type)+"\n";
        return null;
    }
  
   //   /**
   //    * f0 -> "public"
   //    * f1 -> Type()
   //    * f2 -> Identifier()
   //    * f3 -> "("
   //    * f4 -> ( FormalParameterList() )?
   //    * f5 -> ")"
   //    * f6 -> "{"
   //    * f7 -> ( VarDeclaration() )*
   //    * f8 -> ( Statement() )*
   //    * f9 -> "return"
   //    * f10 -> Expression()
   //    * f11 -> ";"
   //    * f12 -> "}"
   //    */
   //   public void visit(MethodDeclaration n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      n.f3.accept(this, file);
   //      n.f4.accept(this, file);
   //      n.f5.accept(this, file);
   //      n.f6.accept(this, file);
   //      n.f7.accept(this, file);
   //      n.f8.accept(this, file);
   //      n.f9.accept(this, file);
   //      n.f10.accept(this, file);
   //      n.f11.accept(this, file);
   //      n.f12.accept(this, file);
   //      return _ret;
   //   }
  
   //   /**
   //    * f0 -> FormalParameter()
   //    * f1 -> FormalParameterTail()
   //    */
   //   public void visit(FormalParameterList n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      return _ret;
   //   }
  
   //   /**
   //    * f0 -> Type()
   //    * f1 -> Identifier()
   //    */
   //   public void visit(FormalParameter n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      return _ret;
   //   }
  
   //   /**
   //    * f0 -> ( FormalParameterTerm() )*
   //    */
   //   public void visit(FormalParameterTail n, String argu) {
   //      return n.f0.accept(this, file);
   //   }
  
   //   /**
   //    * f0 -> ","
   //    * f1 -> FormalParameter()
   //    */
   //   public void visit(FormalParameterTerm n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      return _ret;
   //   }
  
    /**
     * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    @Override
    public String visit(Type n, String argu) {
        return n.f0.accept(this, argu);
    }
  
    /**
     * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
    @Override
    public String visit(ArrayType n, String argu) {
        return "int[]";
    }
  
    /**
     * f0 -> "boolean"
    */
    @Override
    public String visit(BooleanType n, String argu) {
        return "boolean";
    }
  
    /**
     * f0 -> "int"
    */
    @Override
    public String visit(IntegerType n, String argu) {
        return "int";
    }
  
    /**
     * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
    @Override
    public String visit(Statement n, String argu) {
        return n.f0.accept(this, argu);
    }
  
   //   /**
   //    * f0 -> "{"
   //    * f1 -> ( Statement() )*
   //    * f2 -> "}"
   //    */
   //   public void visit(Block n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      return _ret;
   //   }
  
    /**
     * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    @Override
    public String visit(AssignmentStatement n, String argu) {
        
        String ident = n.f0.accept(this, argu);
        String varType = getMethodVarType(ident);

        String targetRegister = "%"+ident;

        String val = n.f2.accept(this, argu);

        // // if val is local var or argument
        // if (val.charAt(0) == '%' && val.charAt(1) != '_' && val != "%this") {
        //     String register = newRegister();
        //     String name = val.substring(1);
        //     String type = getMethodVarType(name);
            
        //     this.buffer += "\t"+register+" = load "+type+", "+type+"* "+val+"\n";
        //     val = register;
        // }
        // // if val is a statement
        // else if (val.startsWith("%_")) {
        //     varType = symbols.currentClass.fields.get(ident);
        //     Integer offset = getOffset(ident);
        //     String register = newRegister();
        //     this.buffer += "\t"+register+" = getelementptr i8, i8* %this, i32 "+offset+"\n";
        //     String register2 = newRegister();
        //     this.buffer += "\t"+register2+" = bitcast i8* "+register+" to "+varType+"*\n";
        //     ident = register2;
        // }
        this.buffer += "\tstore "+LLtype(varType)+" "+val+", "+LLtype(varType)+"* "+targetRegister+"\n";

        return null;
    }
  
   //   /**
   //    * f0 -> Identifier()
   //    * f1 -> "["
   //    * f2 -> Expression()
   //    * f3 -> "]"
   //    * f4 -> "="
   //    * f5 -> Expression()
   //    * f6 -> ";"
   //    */
   //   public void visit(ArrayAssignmentStatement n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      n.f3.accept(this, file);
   //      n.f4.accept(this, file);
   //      n.f5.accept(this, file);
   //      n.f6.accept(this, file);
   //      return _ret;
   //   }
  
    /**
     * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    @Override
    public String visit(IfStatement n, String argu) {
        String exp = n.f2.accept(this, argu);
        String targetRegister = exp;
        
        if (exp.charAt(0) == '%' && !(exp.charAt(1) == '_')) {
            targetRegister = newRegister();
            this.buffer += "\t"+targetRegister+" = load i1, i1* "+exp+"\n";
        }

        String if_then = "if_then_"+this.ifCounter;
        String if_else = "if_else_"+this.ifCounter;
        String if_end = "if_end_"+this.ifCounter;
        this.ifCounter += 1;

        this.buffer += "\tbr i1 "+targetRegister+", label %"+if_then+", label %"+if_else+"\n\n";

        this.buffer += "\t"+if_else+":\n";
        n.f6.accept(this, argu);
        this.buffer += "\tbr label %"+if_end+"\n\n";

        this.buffer += "\t"+if_then+":\n";
        n.f4.accept(this, argu);
        this.buffer += "\tbr label %"+if_end+"\n\n";

        this.buffer += "\t"+if_end+":"+"\n";
        return null;
    }
  
   //   /**
   //    * f0 -> "while"
   //    * f1 -> "("
   //    * f2 -> Expression()
   //    * f3 -> ")"
   //    * f4 -> Statement()
   //    */
   //   public void visit(WhileStatement n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      n.f3.accept(this, file);
   //      n.f4.accept(this, file);
   //      return _ret;
   //   }
  
    /**
     * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    public String visit(PrintStatement n, String argu) {
        String sourceRegister = n.f2.accept(this, argu);

        String targetRegister = sourceRegister;
        // if it was a statement, copy value to 
        if (sourceRegister.charAt(0) == '%' && !(sourceRegister.charAt(1) == '_')) {
            targetRegister = newRegister();
            this.buffer += "\t"+targetRegister+" = load i32, i32* "+sourceRegister+"\n";
        }

        this.buffer += "\tcall void (i32) @print_int(i32 "+targetRegister+")\n";
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
    public String visit(Expression n, String argu) {
        return n.f0.accept(this, argu);
    }
  
   //   /**
   //    * f0 -> Clause()
   //    * f1 -> "&&"
   //    * f2 -> Clause()
   //    */
   //   public void visit(AndExpression n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      return _ret;
   //   }
  
    /**
     * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(CompareExpression n, String argu) {
        String exp1 = n.f0.accept(this, argu);
        if (exp1.charAt(0) == '%' && !(exp1.charAt(1) == '_')) {
            String register1 = newRegister();
            this.buffer += "\t"+register1+" = load i32, i32* "+exp1+"\n";
            exp1 = register1;
        }

        String exp2 = n.f2.accept(this, argu);
        if (exp2.charAt(0) == '%' && !(exp2.charAt(1) == '_')) {
            String register2 = newRegister();
            this.buffer += "\t"+register2+" = load i32, i32* "+exp2+"\n";
            exp2 = register2;
        }

        String targetRegister = newRegister();
        this.buffer += "\t"+targetRegister+" = icmp slt i32 "+exp1+", "+exp2+"\n";
        // this.is_bool = "yes"; //TODO remove ?
        return targetRegister;
    }
  
    /**
     * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(PlusExpression n, String argu) {
        String exp1 = n.f0.accept(this, argu);
        if (exp1.charAt(0) == '%' && !(exp1.charAt(1) == '_')) {
            String register1 = newRegister();
            this.buffer += "\t"+register1+" = load i32, i32* "+exp1+"\n";
            exp1 = register1;
        }

        String exp2 = n.f2.accept(this, argu);
        if (exp2.charAt(0) == '%' && !(exp2.charAt(1) == '_')) {
            String register2 = newRegister();
            this.buffer += "\t"+register2+" = load i32, i32* "+exp2+"\n";
            exp2 = register2;
        }

        String targetRegister = newRegister();
        this.buffer += "\t"+targetRegister+" = add i32 "+exp1+", "+exp2+"\n";
        return targetRegister;
    }
  
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(MinusExpression n, String argu) {
        String exp1 = n.f0.accept(this, argu);
        if (exp1.charAt(0) == '%' && !(exp1.charAt(1) == '_')) {
            String register1 = newRegister();
            this.buffer += "\t"+register1+" = load i32, i32* "+exp1+"\n";
            exp1 = register1;
        }

        String exp2 = n.f2.accept(this, argu);
        if (exp2.charAt(0) == '%' && !(exp2.charAt(1) == '_')) {
            String register2 = newRegister();
            this.buffer += "\t"+register2+" = load i32, i32* "+exp2+"\n";
            exp2 = register2;
        }

        String targetRegister = newRegister();
        this.buffer += "\t"+targetRegister+" = sub i32 "+exp1+", "+exp2+"\n";
        // this.is_bool = "no"; //TODO remove ?
        return targetRegister;
    }
  
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(TimesExpression n, String argu) {
        String exp1 = n.f0.accept(this, argu);
        if (exp1.charAt(0) == '%' && !(exp1.charAt(1) == '_')) {
            String register1 = newRegister();
            this.buffer += "\t"+register1+" = load i32, i32* "+exp1+"\n";
            exp1 = register1;
        }

        String exp2 = n.f2.accept(this, argu);
        if (exp2.charAt(0) == '%' && !(exp2.charAt(1) == '_')) {
            String register2 = newRegister();
            this.buffer += "\t"+register2+" = load i32, i32* "+exp2+"\n";
            exp2 = register2;
        }

        String targetRegister = newRegister();
        this.buffer += "\t"+targetRegister+" = mul i32 "+exp1+", "+exp2+"\n";
        // this.is_bool = "no"; //TODO remove ?
        return targetRegister;
    }
  
   //   /**
   //    * f0 -> PrimaryExpression()
   //    * f1 -> "["
   //    * f2 -> PrimaryExpression()
   //    * f3 -> "]"
   //    */
   //   public void visit(ArrayLookup n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      n.f3.accept(this, file);
   //      return _ret;
   //   }
  
   //   /**
   //    * f0 -> PrimaryExpression()
   //    * f1 -> "."
   //    * f2 -> "length"
   //    */
   //   public void visit(ArrayLength n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      return _ret;
   //   }
  
   //   /**
   //    * f0 -> PrimaryExpression()
   //    * f1 -> "."
   //    * f2 -> Identifier()
   //    * f3 -> "("
   //    * f4 -> ( ExpressionList() )?
   //    * f5 -> ")"
   //    */
   //   public void visit(MessageSend n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      n.f3.accept(this, file);
   //      n.f4.accept(this, file);
   //      n.f5.accept(this, file);
   //      return _ret;
   //   }
  
   //   /**
   //    * f0 -> Expression()
   //    * f1 -> ExpressionTail()
   //    */
   //   public void visit(ExpressionList n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      return _ret;
   //   }
  
   //   /**
   //    * f0 -> ( ExpressionTerm() )*
   //    */
   //   public void visit(ExpressionTail n, String argu) {
   //      return n.f0.accept(this, file);
   //   }
  
   //   /**
   //    * f0 -> ","
   //    * f1 -> Expression()
   //    */
   //   public void visit(ExpressionTerm n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      return _ret;
   //   }
  
    /**
     * f0 -> NotExpression()
    *       | PrimaryExpression()
    */
    public String visit(Clause n, String argu) {
        return n.f0.accept(this, argu);
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
    @Override
    public String visit(PrimaryExpression n, String argu) {
        String value = n.f0.accept(this, argu);
        // if it is an identifier, return its register
        if (n.f0.which == 3) {
            // this.ident = r; //TODO remove?
            String varType = getMethodVarType(value);
            //if value is not a local var or argument
            if (varType == null) {
                varType = symbols.currentClass.fields.get(value);
                int offset = getOffset(value);
                String register = newRegister();
                buffer += "\t"+register+" = getelementptr i8, i8* %this, i32 "+offset+"\n";
                String register2 = newRegister();
                buffer += "\t"+register2+" = bitcast i8* "+register+" to "+varType+"*"+"\n";
                String register3 = newRegister();
                buffer += "\t"+register3+" = load "+varType+", "+varType+"* "+register2+"\n";
                // if (varType = "boolean") //TODO remove?
                //     this.is_bool = "yes";
                return register3;
            }
            else {
                // if (varType == "boolean") //TODO remove?
                //     this.is_bool = "yes";
                return "%"+value;
            }
        }
        // else register is already calulated
        else
            return value;
    }
  
    /**
     * f0 -> <INTEGER_LITERAL>
    */
    @Override
    public String visit(IntegerLiteral n, String argu) {
        return n.f0.toString();
    }
  
    /**
     * f0 -> "true"
    */
    @Override
    public String visit(TrueLiteral n, String argu) {
        return "1";
    }
  
    /**
     * f0 -> "false"
    */
    @Override
    public String visit(FalseLiteral n, String argu) {
        return "0";
    }
  
    /**
     * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, String argu) {
        return n.f0.toString();
    }
  
    /**
     * f0 -> "this"
    */
    @Override
    public String visit(ThisExpression n, String argu) {
        return "%this";
    }
  
   //   /**
   //    * f0 -> "new"
   //    * f1 -> "int"
   //    * f2 -> "["
   //    * f3 -> Expression()
   //    * f4 -> "]"
   //    */
   //   public void visit(ArrayAllocationExpression n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      n.f3.accept(this, file);
   //      n.f4.accept(this, file);
   //      return _ret;
   //   }
  
   //   /**
   //    * f0 -> "new"
   //    * f1 -> Identifier()
   //    * f2 -> "("
   //    * f3 -> ")"
   //    */
   //   public void visit(AllocationExpression n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      n.f3.accept(this, file);
   //      return _ret;
   //   }
  
   //   /**
   //    * f0 -> "!"
   //    * f1 -> Clause()
   //    */
   //   public void visit(NotExpression n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      return _ret;
   //   }
  
   //   /**
   //    * f0 -> "("
   //    * f1 -> Expression()
   //    * f2 -> ")"
   //    */
   //   public void visit(BracketExpression n, String argu) {
   //      void _ret=null;
   //      n.f0.accept(this, file);
   //      n.f1.accept(this, file);
   //      n.f2.accept(this, file);
   //      return _ret;
   //   }
}
