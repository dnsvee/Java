# Forth interpreter

Attempt to create a Forth interpreter in Java. Some things work. Lots of functionality still missing.

Forth is a programming language with very little syntax and simple semantics. Where other languages have functions or methods Forth has words. Words use arguments amd return values by reading and writing to a globally available stack. Words don't have argument lists and return statements like in other languages. 

Running a Forth command looks like this:
1 2 + puts

This first puts the two numbers on the stack and then it runs the '+' command. This command pops both numbers; add them together; and place the sum back on the stack. The 'puts' word outputs the value to standard output. 

To describe for each word what type of elements it consumes from the stack and what values it pushes it back on the stack and in what order a notation called stack notation will be used. So for the word '+' which sums numbers the following stack notation is used: ( a b -- c ) where a, b are numbers and c is the result of adding a and b. The part before the dashes is the state before calling the word where 'b' is top of the stack and 'a' is the second element on the stack. The part after the dashes is the description of the relevant part of the stack after calling the word. 

This interpreter also has available second stack for use.

The available types in the language are double, long, string, boolean, list, dictionary, anonymous function


## Numbers

Numbers default to double values and can be written using the syntax supported by the Double.valueOf operator defined in the Java reference; 0.01, 1, -3.14 are legal values.

These words that work on numbers are currently defined:

These words only work on numbers:

word	stack notation	explanation
+	( a b -- c )    c == a + b
-	( a b -- c )    c == a - b
*	( a b -- c )	c == a * b
/	( a b -- c )	c == a / b
**	( a b -- c ) 	c == pow(a, b)
sin	( a b -- c )	c = sin(a)
cos	( a b -- c )	c = cos(a)
tan	( a b -- c )	c = tan(a)
abs	( a   -- b )	b = abs(a)
>	( a b -- c )    c:boolean == a > b
<	( a b -- c )    c:boolean == a < b
>=	( a b -- c )    c:boolean == a <= b
<=	( a b -- c )    c:boolean == a >= b


Words that work on all types:

word	stack notation	explanation
==	( a b -- c )	c:boolean == a.equalsTo(b)
puts	( a -- ) 	outputs top stack element as strign to stdout


## Strings

If Forth does not recognize a word it will consider it a string literal. The word " will treat the next word as a string literal. Strings can have embedded spaces in them prefaced with a backslash. Strings support the escape sequences \s and \n.

These are all valid strings:
Hello 
" Hello 
Hello\ world!
Line\sone\nLine\stwo\n

## Stack ordering

Objects on the stack can be rearranged if necessary. Sometimes the order of words must be swapped to accomadate a word.

word	stack notation		explanation
dup 	( a -- a a )		duplicates the top word
swap 	( a b -- b a )		swaps the top two words
drop 	( a -- )		pops the top element
nip  	( a b -- b )    	drops the element below the top element
over 	( a b -- a b a ) 	duplicates the object under the top element

There is also a second stack. The stack notation is extended to read ( a ; c -- c ; a ). This means that the top elements from both stacks are swapped. The part before the semicolon is the main stack. The part after is the second stack.

word	stack notation		explanation

twirl	( a ; b -- b ; a )	swaps top elements of both stack
raise 	( a ; -- ; a )		moves elements from main to second stack
lower	( ; a -- a ; ) 		moves elements from second to main stack

## Lambdas

A lambda or anonymous function groups word together in a single statemen. The lambda can be called using the word call like this:
[ ... do something ... ] call

Lambdas on the stack are represented with numbers and placed by default on the second stack.

## Conditional

The if/then/else construct found in other languages is implemented like this

boolean [ <called when true> ] [ <called when false> ] test 

'test' consumes the boolean on the top of the stack and if it is true it will call the first lambda otherwise it will call the second one. Both lambdas are placed on the second stack so the boolean is is on top of the main stack.

## Looping

The while or for loop construct found in other languages is implemented like this:

[ condition ] [ loop body ] while

The 'while' word will first call the 'condition' lambda and it expects a boolean to be the top stack element.  If it is true it will call the second lambda. When the boolean is false if will end the loop and pop both lambdas from the second stack. 

## Constants

true, false, e, PI

## Lists

A list, implemented using ArrayList, is constructed like this:

( elements... ) 

word	stack notation	explanation

push	( a b -- )	push a on top of b:list
pop	( a -- b )	pops top element from a:list on top of stack as b
size	( a -- b )	b is size of list a:list

## Maps

A map is a dictionary that maps keys to values. A map is created like this:

map!

word	stack notation	explanation

put	( a b c -- )	add pair of b:key and a:value to dictionary c
get	( a b -- c )	get value associated with c:key from a:map and put on stack as c:value
size	( a -- b )	c is size of a:map






