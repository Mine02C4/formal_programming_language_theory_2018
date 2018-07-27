/* tpconst7.c:  Constants (long int, long long int) */

int printf(char*, ...);

int main()
{
  long int long1, long2;
  long long int longlong1, longlong2;
  long1 = 0xe0000000L;  /* This should be a positive constant */ 
          /* This is represented as -536870912 in Coins1.3.2.2 and gcc for x86.
                            Nakata mail 060610 */
  longlong1 = 0xe0000000LL;
  long2 = 0x00000000e0000000L;
  longlong2 = 0x00000000e0000000LL;
/*
  printf("%d %x %d %x\n", long1, long1, longlong1, longlong1);
  printf("%lld %llx %lld %llx\n", long2, long2, longlong2, longlong2);
*/
  printf("%ld %lx %lld %llx\n", long1, long1, longlong1, longlong1);
  printf("%ld %lx %lld %llx\n", long2, long2, longlong2, longlong2);
  return 0;
}

