/* tpcpp2-1.c -- C preprocessor for tolower() including ctype.h */

#include <ctype.h>
int foo(char p)
{
  return tolower(p);
}

int main()
{
  char a;
  printf("%c \n", foo('A'));
  return  0;
}

