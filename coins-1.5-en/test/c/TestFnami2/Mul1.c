int printf(char *s, ...);

char  s9[]="%d %d %d %d %d %d %d %d %d\n";
char s10[]="%d %d %d %d %d %d %d %d %d %d\n";

int a0=10*0,a1=10*1,a2=10*2,a3=10*3,a4=10*4,a5=10*5,a6=10*6,a7=10*0x56,a8=10*0xAB,a9=10*214748364;
int b1=10*(-1),b2=10*(-2),b3=10*(-3),b4=10*(-4),b5=10*(-5),b6=10*(-6),b7=10*(-0x56),b8=10*(-0xAB),b9=10*(-214748364);

void f0(int x) {
  printf(s10,x*0,x*1,x*2,x*3,x*4,x*5,x*6,x*0x56,x*0xAB,x*214748364);
  printf( s9,x*(-1),x*(-2),x*(-3),x*(-4),x*(-5),x*(-6),x*(-0x56),x*(-0xAB),x*(-214748364));
}

void f1(int x) {
  printf(s10,0*x,1*x,2*x,3*x,4*x,5*x,6*x,0x56*x,0xAB*x,214748364*x);
  printf( s9,(-1)*x,(-2)*x,(-3)*x,(-4)*x,(-5)*x,(-6)*x,(-0x56)*x,(-0xAB)*x,(-214748364)*x);
}

int op(int x,int y) { return x*y; }

void f2(int x) {
  printf(s10,op(x,0),op(x,1),op(x,2),op(x,3),op(x,4),op(x,5),op(x,6),op(x,0x56),op(x,0xAB),op(x,214748364));
  printf( s9,op(x,-1),op(x,-2),op(x,-3),op(x,-4),op(x,-5),op(x,-6),op(x,-0x56),op(x,-0xAB),op(x,-214748364));
}

int main() {
  int x;

  printf(s10,a0,a1,a2,a3,a4,a5,a6,a7,a8,a9);
  printf( s9,b1,b2,b3,b4,b5,b6,b7,b8,b9);

  printf(s10,10*0,10*1,10*2,10*3,10*4,10*5,10*6,10*0x56,10*0xAB,10*214748364);
  printf( s9,10*(-1),10*(-2),10*(-3),10*(-4),10*(-5),10*(-6),10*(-0x56),10*(-0xAB),10*(-214748364));
  x=10;
  printf(s10,x*0,x*1,x*2,x*3,x*4,x*5,x*6,x*0x56,x*0xAB,x*214748364);
  printf(s10,x*0,x*1,x*2,x*3,x*4,x*5,x*6,x*0x56,x*0xAB,x*214748364);
  x=10;
  printf( s9,x*(-1),x*(-2),x*(-3),x*(-4),x*(-5),x*(-6),x*(-0x56),x*(-0xAB),x*(-214748364));
  printf( s9,x*(-1),x*(-2),x*(-3),x*(-4),x*(-5),x*(-6),x*(-0x56),x*(-0xAB),x*(-214748364));
  x=10;
  printf(s10,0*x,1*x,2*x,3*x,4*x,5*x,6*x,0x56*x,0xAB*x,214748364*x);
  printf(s10,0*x,1*x,2*x,3*x,4*x,5*x,6*x,0x56*x,0xAB*x,214748364*x);
  x=10;
  printf( s9,(-1)*x,(-2)*x,(-3)*x,(-4)*x,(-5)*x,(-6)*x,(-0x56)*x,(-0xAB)*x,(-214748364)*x);
  printf( s9,(-1)*x,(-2)*x,(-3)*x,(-4)*x,(-5)*x,(-6)*x,(-0x56)*x,(-0xAB)*x,(-214748364)*x);

  f0(10); f0(-10);
  f1(10); f1(-10);
  f2(10); f2(-10);

  return 0;
}
