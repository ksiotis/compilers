import syntaxtree.*;
import visitor.GJDepthFirst;
import java.io.BufferedWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class LLVMVisitor extends GJDepthFirst<String, String> {
    SymbolTable symbols;
    ClassOffsetsContainer offsets;
    BufferedWriter file;
    Integer registerCounter = 0;
    Integer ifCounter = 0;
    Integer loopCounter = 0;
    Integer randLabelCounter = 0;
    String buffer = "";
    ArrayList<String> parameters = new ArrayList<String>();

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
            case "int[]":
                ret = "i32*";
                break;
            case "boolean":
                ret = "i1";
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

    String newLabel() {
        String label = "%Label_"+this.randLabelCounter;
        this.randLabelCounter++;
        return label;
    }

    void resetCounters() {
        this.registerCounter = 0;
        this.ifCounter = 0;
        this.loopCounter = 0;
        this.randLabelCounter = 0;
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

    public Integer getMethodOffset(String className, String methodName) {
        ClassTable currentClass = symbols.classes.get(className);
        while (currentClass != null) {
            Integer ret = offsets.offsets.get(currentClass.name).methodOffsets.get(methodName);
            if (ret != null) {
                return ret;
            }
            currentClass = currentClass.parent;
        }
        return -1;
    }

    public Integer getVarOffset(String className, String varName) {
        ClassTable currentClass = symbols.classes.get(className);
        while (currentClass != null) {
            Integer ret = offsets.offsets.get(currentClass.name).variableOffsets.get(varName);
            if (ret != null) {
                return ret;
            }
            currentClass = currentClass.parent;
        }
        return -1;
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
        
        
        String value = n.f2.accept(this, argu);
        // if value is register load it
        // if (value.charAt(0) == '%' && value.charAt(1) != '_' && value != "%this") { //TODO remove ?
        if (value.charAt(0) == '%' && value.charAt(1) != '_') {
            String register = newRegister();
            String name = value.substring(1);
            String type = getMethodVarType(name);
            
            this.buffer += "\t"+register+" = load "+LLtype(type)+", "+LLtype(type)+"* "+value+"\n";
            value = register;
        }
        
        String ident = n.f0.accept(this, argu);
        String varType = getMethodVarType(ident);
        // if not locally look in class
        if (varType == null) {
            varType = symbols.currentClass.fields.get(value);
            int offset = getOffset(value);
            String register = newRegister();
            this.buffer += "\t"+register+" = getelementptr i8, i8* %this, i32 "+offset+"\n";
            String register2 = newRegister();
            this.buffer += "\t"+register2+" = bitcast i8* "+register+" to "+varType+"*"+"\n";
            ident = register2;
        }
        else {
            ident = "%"+ident;
        }
        this.buffer += "\tstore "+LLtype(varType)+" "+value+", "+LLtype(varType)+"* "+ident+"\n";

        return value;
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
    @Override
    public String visit(ArrayAssignmentStatement n, String argu) {
        String value = n.f0.accept(this, argu);
        String varType = getMethodVarType(value);
        String table = newRegister();
        // if not locally look in class
        if (varType == null) {
            varType = symbols.currentClass.fields.get(value);
            int offset = getOffset(value);
            String register = newRegister();
            buffer += "\t"+register+" = getelementptr i8, i8* %this, i32 "+offset+"\n";
            String register2 = newRegister();
            buffer += "\t"+register2+" = bitcast i8* "+register+" to "+varType+"*"+"\n";
            buffer += "\t"+table+" = load "+varType+", "+varType+"* "+register2+"\n";
        }
        // else if in var just load it
        else {
            this.buffer += "\t"+table+" = load i32*, i32** %"+value+"\n";
        }
        String size = newRegister();
        this.buffer += "\t"+size+" = load i32, i32* "+table+"\n";

        String index = n.f2.accept(this, argu);
        if (index.charAt(0) == '%' && !(index.charAt(1) == '_')) {
            String register1 = newRegister();
            this.buffer += "\t"+register1+" = load i32, i32* "+index+"\n";
            index = register1;
        }
        
        // OOP check
        String oob_ok = newLabel();
        String oob_err = newLabel();
        String check1 = newRegister(); // 0 <= index
        this.buffer += "\t"+check1+" = icmp sle i32 0, "+index+"\n";
        String check2 = newRegister(); // index < size
        this.buffer += "\t"+check2+" = icmp slt i32 "+index+", "+size+"\n";
        String checks = newRegister();
        this.buffer += "\t"+checks+" = and i1 "+check1+", "+check2+"\n";
        this.buffer += "\tbr i1 "+checks+", label "+oob_ok+", label "+oob_err+"\n";

        this.buffer += "\n\t"+oob_err+":\n";
        this.buffer += "\tcall void @throw_oob()\n";
        this.buffer += "\tbr label "+oob_ok+"\n";

        this.buffer += "\n\t"+oob_ok+":\n";
        String actualIndex = newRegister();
        this.buffer += "\t"+actualIndex+" = add i32 1, "+index+"\n";
        String elementPtr = newRegister();
        this.buffer += "\t"+elementPtr+" = getelementptr i32, i32* "+table+", i32 "+actualIndex+"\n"; 
        // store to elementPtr address
        String tagetValue = n.f5.accept(this, argu);
        this.buffer += "\tstore i32 "+tagetValue+", i32* "+elementPtr+"\n";

        return elementPtr; //not needed but meh
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
    @Override
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
    @Override
    public String visit(Expression n, String argu) {
        return n.f0.accept(this, argu);
    }
  
    /**
     * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    @Override
    public String visit(AndExpression n, String argu) {
        String exp1 = n.f0.accept(this, argu);
        // if exp1 returned register load its value
        if (exp1.charAt(0) == '%' && !(exp1.charAt(1) == '_')) {
            String register1 = newRegister();
            this.buffer += "\t"+register1+" = load i1, i1* "+exp1+"\n";
            exp1 = register1;
        }

        // Check result, short circuit if false
        String endLablel = newLabel();
        String endLablel_forphi = newLabel();
        String nextLablel = newLabel();
        String nextLablel_forphi = newLabel();

        // if exp1 true continue, else jump to end
        this.buffer += "\tbr i1 "+exp1+", label "+nextLablel+", label "+endLablel_forphi+"\n";
        
        this.buffer += "\n\t"+endLablel_forphi+":\n";
        this.buffer += "\tbr label "+endLablel+":\n";
        
        // continue 
        this.buffer += "\n\t"+nextLablel+":\n";
        String exp2 = n.f2.accept(this, argu);
        // if exp2 returned register load its value
        if (exp2.charAt(0) == '%' && !(exp2.charAt(1) == '_')) {
            String register2 = newRegister();
            this.buffer += "\t"+register2+" = load i1, i1* "+exp2+"\n";
            exp2 = register2;
        }
        this.buffer += "\tbr label "+nextLablel_forphi+":\n";

        this.buffer += "\n\t"+nextLablel_forphi+":\n";
        this.buffer += "\tbr label "+endLablel+":\n";

        String targetRegister = newRegister();
        this.buffer += "\n\t"+endLablel+":\n";
        this.buffer += "\t"+targetRegister+" = phi i1  [ 0, "+endLablel_forphi+" ], [ "+exp2+", "+nextLablel_forphi+" ]\n";
        // // this.is_bool = "yes"; //TODO remove ?
        return targetRegister;
    }
  
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
  
    /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    @Override
    public String visit(ArrayLookup n, String argu) {
        String table = n.f0.accept(this, argu);
        if (table.charAt(0) == '%' && !(table.charAt(1) == '_')) {
            String register1 = newRegister();
            this.buffer += "\t"+register1+" = load i32*, i32** "+table+"\n";
            table = register1;
        }
        String size = newRegister();
        this.buffer += "\t"+size+" = load i32, i32* "+table+"\n";
        
        String index = n.f2.accept(this, argu);
        if (index.charAt(0) == '%' && !(index.charAt(1) == '_')) {
            String register1 = newRegister();
            this.buffer += "\t"+register1+" = load i32, i32* "+index+"\n";
            index = register1;
        }
        
        // OOP check
        String oob_ok = newLabel();
        String oob_err = newLabel();
        String check1 = newRegister(); // 0 <= index
        this.buffer += "\t"+check1+" = icmp sle i32 0, "+index+"\n";
        String check2 = newRegister(); // index < size
        this.buffer += "\t"+check2+" = icmp slt i32 "+index+", "+size+"\n";
        String checks = newRegister();
        this.buffer += "\t"+checks+" = and i1 "+check1+", "+check2+"\n";
        this.buffer += "\tbr i1 "+checks+", label "+oob_ok+", label "+oob_err+"\n";

        this.buffer += "\n\t"+oob_err+":\n";
        this.buffer += "\tcall void @throw_oob()\n";
        this.buffer += "\tbr label "+oob_ok+"\n";

        this.buffer += "\n\t"+oob_ok+":\n";
        String actualIndex = newRegister();
        this.buffer += "\t"+actualIndex+" = add i32 1, "+index+"\n";
        String elementPtr = newRegister();
        this.buffer += "\t"+elementPtr+" = getelementptr i32, i32* "+table+", i32 "+actualIndex+"\n";

        String value = newRegister();
        this.buffer += "\t"+value+" = load i32, i32* "+elementPtr+"\n";
        return value;
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
    public String visit(ArrayLength n, String argu) {
        String table = n.f0.accept(this, argu);
        if (table.charAt(0) == '%' && !(table.charAt(1) == '_')) {
            String register1 = newRegister();
            this.buffer += "\t"+register1+" = load i32*, i32** "+table+"\n";
            table = register1;
        }
        String size = newRegister();
        this.buffer += "\t"+size+" = load i32, i32* "+table+"\n";

        return size;
    }
  
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    @Override
    public String visit(MessageSend n, String argu) {
        String object = n.f0.accept(this, argu);
        String objectName = object.substring(1);
        String objectType = getMethodVarType(objectName);

        // if a class or a var or a register, load its value
        if (objectType != null && object.charAt(0) == '%' && !(object.charAt(1) == '_')) {
            String register1 = newRegister();
            this.buffer += "\t"+register1+" = load "+LLtype(objectType)+", "+LLtype(objectType)+"* "+object+"\n";
            object = register1;
        }
        // else { // get class field <- not needed, here it can only be a method }

        String objMethod = n.f2.accept(this, argu);

        this.parameters.clear();
        if (n.f4.present()) {
            n.f4.accept(this, argu); // will load parameters
        }

        Integer offset = symbols.getOffsets().get(objectType).methodOffsets.get(objMethod);
        this.buffer += "\t; "+objectType+"."+objMethod+" : "+offset+"\n"; //TODO remove

        // Do the required bitcasts, so that we can access the vtable pointer
        String cast = newRegister();
        this.buffer += "\t"+cast+" = bitcast i8* "+object+" to i8***\n";
        // Load vtable_ptr
        String vtable_ptr = newRegister();
        this.buffer += "\t"+vtable_ptr+" = load i8**, i8*** "+cast+"\n";
        String elementPtr = newRegister();
        this.buffer += "\t"+elementPtr+" = getelementptr i8*, i8** "+vtable_ptr+", i32 "+offset+"\n";
        String actualPointer = newRegister();
        this.buffer += "\t"+actualPointer+" = load i8*, i8** "+elementPtr+"\n";
        // Cast the function pointer from i8* to a function ptr type that matches its signature.
        String cast2 = newRegister();
        String methodType = symbols.classes.get(objectType).methods.get(objMethod).type;
        String paramTypes = "";
        for (String type : symbols.classes.get(objectType).methods.get(objMethod).args.values()) {
            paramTypes += ","+LLtype(type);
        }
        this.buffer += "\t"+cast2+" = bitcast i8* "+actualPointer+" to "+LLtype(methodType)+" (i8*"+paramTypes+")*\n";
        //load values of parameters
        String callParams = "";
        String[] paramTypesArray = paramTypes.split(",");
        for (int i = 0; i < this.parameters.size(); i++) {
            String param = this.parameters.get(i);
            if (param.charAt(0) == '%' && !(param.charAt(1) == '_') && param != "%this") {
                String paramReg = newRegister();
                this.buffer += "\t"+paramReg+" = load "+LLtype(paramTypesArray[i])+", "+LLtype(paramTypesArray[i+1])+"* "+param+"\n"; // small hack because of split(",")
                param = paramReg;
            }

            callParams += ", "+paramTypesArray[i+1]+" "+param; // small hack because of split(",")
        }
        // Perform the call
        String call = newRegister();
        this.buffer += "\t"+call+" = call "+LLtype(methodType)+" "+cast2+"(i8* "+object+callParams+")\n";

        return call;
    }
  
    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    @Override
    public String visit(ExpressionList n, String argu) {
        String exp = n.f0.accept(this, argu);
        this.parameters.add(exp);
        n.f1.accept(this, argu);
        return argu;
    }
  
    /**
     * f0 -> ( ExpressionTerm() )*
    */
    @Override
    public String visit(ExpressionTail n, String argu) {
        String exp = n.f0.accept(this, argu);
        if (exp != null) this.parameters.add(exp);
        return argu;
    }
  
    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    @Override
    public String visit(ExpressionTerm n, String argu) {
        return n.f0.accept(this, argu);
    }
  
    /**
     * f0 -> NotExpression()
    *       | PrimaryExpression()
    */
    @Override
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
                this.buffer += "\t"+register+" = getelementptr i8, i8* %this, i32 "+offset+"\n";
                String register2 = newRegister();
                this.buffer += "\t"+register2+" = bitcast i8* "+register+" to "+varType+"*"+"\n";
                String register3 = newRegister();
                this.buffer += "\t"+register3+" = load "+varType+", "+varType+"* "+register2+"\n";
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
  
  /**
   * f0 -> "new"
   * f1 -> "int"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  @Override
  public String visit(ArrayAllocationExpression n, String argu) {
    String exp = n.f3.accept(this, argu);
    // if register, load its value
    if (exp.charAt(0) == '%' && !(exp.charAt(1) == '_')) {
        String register = newRegister();
        this.buffer += "\t"+register+" = load i32, i32* "+exp+"\n";
        exp = register;
    }
    // calculate size bytes to be allocated for the array
    String size = newRegister();
    this.buffer += "\t"+size+" = add "+exp+", 1\n";

    // check that the size of the array is >= 1
    String check = newRegister();
    this.buffer += "\t"+check+" = icmp sge i32 "+size+", 1\n";
    String okLabel = newLabel();
    String badSizeLabel = newLabel();
    this.buffer += "\tbr i1 "+check+", label "+okLabel+", label "+badSizeLabel+"\n";

    // badSizeLabel
    this.buffer += "\n\t"+badSizeLabel+":\n";
    this.buffer += "\tcall void @throw_nsz()\n";
    this.buffer += "\tbr label "+okLabel+"\n";

    // okLabel
    this.buffer += "\n\t"+okLabel+":\n";

    // allocation
    String func = newRegister();
    this.buffer += "\t"+func+" = call i8* @calloc(i32 "+size+", i32 4)\n";
    // cast
    String cast = newRegister();
    this.buffer += "\t"+cast+" = bitcast i8* "+func+" to i32*\n";
    //store the size of the array in the first position of the array
    this.buffer += "\tstore i32 "+size+", i32* "+cast+"\n";

    return cast;
  }
  
    /**
     * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    @Override
    public String visit(AllocationExpression n, String argu) {
        String ident = n.f1.accept(this, argu);

        // allocate the required memory on heap
        Integer classSize = offsets.offsets.get(ident).varOffset + 8;
        String allocate = newRegister();
        this.buffer += "\t"+allocate+" = call i8* @calloc(i32 1, i32 "+classSize+")\n";

        // set the vtable pointer to point to the correct vtable
        String cast = newRegister();
        this.buffer += "\t"+cast+" = bitcast i8* "+allocate+" to i8***\n";
        Integer methodNum = offsets.offsets.get(ident).methodOffset / 8;
        String myvtable = newRegister();
        this.buffer += "\t"+myvtable+" = getelementptr ["+methodNum+" x i8*], ["+methodNum+
                                    " x i8*]* @."+ident+"_vtable, i32 0, i32 0\n";
        // set the vtable to the correct address
        this.buffer += "\tstore i8** "+myvtable+", i8*** "+cast+"\n";

        return allocate;
    }
  
    /**
     * f0 -> "!"
    * f1 -> Clause()
    */
    @Override
    public String visit(NotExpression n, String argu) {
        String val = n.f1.accept(this, argu);
        // if (!c.startsWith("%_") && c.startsWith("%") && c != "%this"){ //TODO remove
        // if register, load its value
        if (val.charAt(0) == '%' && !(val.charAt(1) == '_')) {
            String register = newRegister();
            this.buffer += "\t"+register+" = load i1, i1* "+val+"\n";
            val = register;
        }
        String register = newRegister();
        this.buffer += "\t"+register+" = xor i1 1, "+val+"\n";
        return register;
    }
  
    /**
     * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    @Override
    public String visit(BracketExpression n, String argu) {
        return n.f1.accept(this, argu);
    }
}
