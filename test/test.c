
int main() {
    int x, y, z;
    x = 1;
    y = x + 3;
    z = x + 3;
    printf("x,y,z = %d, %d, %d\n", x, y, z);
    y = 5;
    y = y + 3;
    z = y + 3;
    printf("x,y,z = %d, %d, %d\n", x, y, z);
}
