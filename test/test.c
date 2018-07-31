#include <stdio.h>

int global;

int main() {
    int x, y, z, i, j, k;
    int array[10];
    x = 1;
    y = x + 3;
    z = x + 3;
    printf("x,y,z = %d, %d, %d\n", x, y, z);
    y = 5;
    y = y + 3;
    z = y + 3;
    printf("x,y,z = %d, %d, %d\n", x, y, z);
    z = x + 3;
    x = 3 * 10;
    y = y * 3;
    y = x + 3;
    printf("x,y,z = %d, %d, %d\n", x, y, z);
    global = x + 5;
    array[0] = x + 5;
    i = 10;
    j = i * x;
    k = j + z;
    if (global > array[0]) {
      printf("global > array[0] : %d, %d\n", global, array[0]);
    } else {
      printf("global <= array[0] : %d, %d\n", global, array[0]);
    }
    return 0;
}

