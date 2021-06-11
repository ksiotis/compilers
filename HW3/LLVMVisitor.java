import java.io.BufferedWriter;

public class LLVMVisitor extends GJDepthFirst<void, BufferedWriter> {
    SymbolTable symbols;
    ClassOffsetsContainer offsets;

    void setOffsets(SymbolTable arg1, ClassOffsetsContainer arg2) {
        this.symbols = arg1;
        this.offsets = arg2;
    }

    /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    @Override
    public void visit(Goal n, BufferedWriter file) {
        n.f0.accept(this, file);
        n.f1.accept(this, file);
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
     public void visit(MainClass n, BufferedWriter file) {
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        n.f4.accept(this, file);
        n.f5.accept(this, file);
        n.f6.accept(this, file);
        n.f7.accept(this, file);
        n.f8.accept(this, file);
        n.f9.accept(this, file);
        n.f10.accept(this, file);
        n.f11.accept(this, file);
        n.f12.accept(this, file);
        n.f13.accept(this, file);
        n.f14.accept(this, file);
        n.f15.accept(this, file);
        n.f16.accept(this, file);
        n.f17.accept(this, file);
     }
  
     /**
      * f0 -> ClassDeclaration()
      *       | ClassExtendsDeclaration()
      */
     public void visit(TypeDeclaration n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> "class"
      * f1 -> Identifier()
      * f2 -> "{"
      * f3 -> ( VarDeclaration() )*
      * f4 -> ( MethodDeclaration() )*
      * f5 -> "}"
      */
     public void visit(ClassDeclaration n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        n.f4.accept(this, file);
        n.f5.accept(this, file);
        return _ret;
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
     public void visit(ClassExtendsDeclaration n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        n.f4.accept(this, file);
        n.f5.accept(this, file);
        n.f6.accept(this, file);
        n.f7.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> Type()
      * f1 -> Identifier()
      * f2 -> ";"
      */
     public void visit(VarDeclaration n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        return _ret;
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
     public void visit(MethodDeclaration n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        n.f4.accept(this, file);
        n.f5.accept(this, file);
        n.f6.accept(this, file);
        n.f7.accept(this, file);
        n.f8.accept(this, file);
        n.f9.accept(this, file);
        n.f10.accept(this, file);
        n.f11.accept(this, file);
        n.f12.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> FormalParameter()
      * f1 -> FormalParameterTail()
      */
     public void visit(FormalParameterList n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> Type()
      * f1 -> Identifier()
      */
     public void visit(FormalParameter n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> ( FormalParameterTerm() )*
      */
     public void visit(FormalParameterTail n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> ","
      * f1 -> FormalParameter()
      */
     public void visit(FormalParameterTerm n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> ArrayType()
      *       | BooleanType()
      *       | IntegerType()
      *       | Identifier()
      */
     public void visit(Type n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> "int"
      * f1 -> "["
      * f2 -> "]"
      */
     public void visit(ArrayType n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> "boolean"
      */
     public void visit(BooleanType n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> "int"
      */
     public void visit(IntegerType n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> Block()
      *       | AssignmentStatement()
      *       | ArrayAssignmentStatement()
      *       | IfStatement()
      *       | WhileStatement()
      *       | PrintStatement()
      */
     public void visit(Statement n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> "{"
      * f1 -> ( Statement() )*
      * f2 -> "}"
      */
     public void visit(Block n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> Identifier()
      * f1 -> "="
      * f2 -> Expression()
      * f3 -> ";"
      */
     public void visit(AssignmentStatement n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        return _ret;
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
     public void visit(ArrayAssignmentStatement n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        n.f4.accept(this, file);
        n.f5.accept(this, file);
        n.f6.accept(this, file);
        return _ret;
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
     public void visit(IfStatement n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        n.f4.accept(this, file);
        n.f5.accept(this, file);
        n.f6.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> "while"
      * f1 -> "("
      * f2 -> Expression()
      * f3 -> ")"
      * f4 -> Statement()
      */
     public void visit(WhileStatement n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        n.f4.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> "System.out.println"
      * f1 -> "("
      * f2 -> Expression()
      * f3 -> ")"
      * f4 -> ";"
      */
     public void visit(PrintStatement n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        n.f4.accept(this, file);
        return _ret;
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
     public void visit(Expression n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> Clause()
      * f1 -> "&&"
      * f2 -> Clause()
      */
     public void visit(AndExpression n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "<"
      * f2 -> PrimaryExpression()
      */
     public void visit(CompareExpression n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "+"
      * f2 -> PrimaryExpression()
      */
     public void visit(PlusExpression n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "-"
      * f2 -> PrimaryExpression()
      */
     public void visit(MinusExpression n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "*"
      * f2 -> PrimaryExpression()
      */
     public void visit(TimesExpression n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "["
      * f2 -> PrimaryExpression()
      * f3 -> "]"
      */
     public void visit(ArrayLookup n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "."
      * f2 -> "length"
      */
     public void visit(ArrayLength n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "."
      * f2 -> Identifier()
      * f3 -> "("
      * f4 -> ( ExpressionList() )?
      * f5 -> ")"
      */
     public void visit(MessageSend n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        n.f4.accept(this, file);
        n.f5.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> Expression()
      * f1 -> ExpressionTail()
      */
     public void visit(ExpressionList n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> ( ExpressionTerm() )*
      */
     public void visit(ExpressionTail n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> ","
      * f1 -> Expression()
      */
     public void visit(ExpressionTerm n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> NotExpression()
      *       | PrimaryExpression()
      */
     public void visit(Clause n, A file) {
        return n.f0.accept(this, file);
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
     public void visit(PrimaryExpression n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> <INTEGER_LITERAL>
      */
     public void visit(IntegerLiteral n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> "true"
      */
     public void visit(TrueLiteral n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> "false"
      */
     public void visit(FalseLiteral n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> <IDENTIFIER>
      */
     public void visit(Identifier n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> "this"
      */
     public void visit(ThisExpression n, A file) {
        return n.f0.accept(this, file);
     }
  
     /**
      * f0 -> "new"
      * f1 -> "int"
      * f2 -> "["
      * f3 -> Expression()
      * f4 -> "]"
      */
     public void visit(ArrayAllocationExpression n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        n.f4.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> "new"
      * f1 -> Identifier()
      * f2 -> "("
      * f3 -> ")"
      */
     public void visit(AllocationExpression n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        n.f3.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> "!"
      * f1 -> Clause()
      */
     public void visit(NotExpression n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        return _ret;
     }
  
     /**
      * f0 -> "("
      * f1 -> Expression()
      * f2 -> ")"
      */
     public void visit(BracketExpression n, A file) {
        void _ret=null;
        n.f0.accept(this, file);
        n.f1.accept(this, file);
        n.f2.accept(this, file);
        return _ret;
     }
}
