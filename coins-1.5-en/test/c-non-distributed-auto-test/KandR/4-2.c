#include <ctype.h>

double atof(char s[])
{
	double val, power;
	int i, sign;

	for (i = 0; isspace(s[i]); i++)
		;
	sign = (s[i] == '-') ? -1 : 1;
	if (s[i] == '+' || s[i] == '-')
		i++;
	for (val = 0.0; isdigit(s[i]); i++)
		val = 10.0 * val + (s[i] - '0');
	if (s[i] == '.')
		i++;
	for (power = 1.0; isdigit(s[i]); i++) {
		val = 10.0 * val + (s[i] - '0');
		power *= 10.0;
	}
	return sign * val / power;
}

#include <stdio.h>

#define MAXLINE 100

main()
{
	double sum, atof(char []);
	char line[MAXLINE];
	int getline(char line[], int max);

	sum = 0;
	while (getline(line, MAXLINE) > 0)
		printf("\t%g\n", sum += atof(line));
	return 0;
}

int getline(char s[], int lim)
{
	int c, i;

	for (i=0; i<lim-1 && (c=getchar())!=EOF && c!='\n'; ++i)
		s[i] = c;
	if (c == '\n') {
		s[i] = c;
		++i;
	}
	s[i] = '\0';
	return i;
}

int atoi(char s[])
{
	double atof(char s[]);
	return (int) atof(s);
}
