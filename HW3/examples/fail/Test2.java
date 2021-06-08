class Test2 {
  public static void main(String[] args) {}
}

class A {
  // Error : double declaration of symbol x inside argument list
  public int foo(A x, int x) {
    return 5;
  }
}