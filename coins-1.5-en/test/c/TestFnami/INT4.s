 .ident "Coins Compiler version: coins-1.4.3.1 + BackEnd-1.0"
/* JavaCG for target:x86 convention:cygwin */
	.text
	.align	1
_string.7:
	.byte	37
	.byte	100
	.byte	10
	.byte	0

	.align	4
	.global	_main
_main:
	pushl	%ebp
	movl	%esp,%ebp
	movl	$2147483647,%eax
	movl	$-2147483648,%eax
	movl	$-2147483648,%eax
	movl	$2147483647,%eax
	pushl	$2147483647
	pushl	$_string.7
	call	_printf
	leal	8(%esp),%esp
	pushl	$-2147483648
	pushl	$_string.7
	call	_printf
	leal	8(%esp),%esp
	pushl	$-2147483648
	pushl	$_string.7
	call	_printf
	leal	8(%esp),%esp
	pushl	$2147483647
	pushl	$_string.7
	call	_printf
	leal	8(%esp),%esp
	movl	$0,%eax
	leave
	ret

