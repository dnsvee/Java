# Forth interpreter

Attempt to create a Forth interpreter in Java. Some things work. Lots of functionality still missing.

Forth is a programming language with very little syntax and simple semantics. Where other languages have functions or methods Forth has words. Words use arguments amd return values by reading and writing to a globally available stack. Words don't have argument lists and return statements like in other languages. 

Running a Forth command looks like this:
1 2 + puts

This first puts the two numbers on the stack and then it runs the '+' command. This command pops both numbers from the stack numbers and adds them together; the sum is placed on top of the stack. The 'puts' word outputs the value to standard output. 

To describe for each word what type of elements it consumes from the stack and what values it pushes back on the stack; and in what representation called stack notation will be used. So for the word '+' which sums numbers the following stack notation is used: ( a b -- c ) where a, b are numbers and c is the result of adding a and b. The part before the dashes is the state of the stack before calling the word where 'b' is top of the stack and 'a' is below it. The part after the dashes is the description of top of the stack of the elements put their by the word.

This interpreter also has available a second stack for use.

The available types in the language are double, long, string, boolean, list, map, set.

## Numbers

Numbers by default are double values and can be written using the syntax supported by the Double.valueOf operator described in the Java reference.

Legal values are
```
0.01
1
-3.14 
```

Here is a list of words that only work on numbers. Other words can also be used on numbers and will be described later.

```
word	stack notation	explanation
+	( a b -- c )    c is a + b
-	( a b -- c )    c is a - b
*	( a b -- c )	c is a * b
/	( a b -- c )	c is a / b
**	( a b -- c ) 	c is pow(a, b)
sin	( a b -- c )	c is sin(a)
cos	( a b -- c )	c is cos(a)
tan	( a b -- c )	c is tan(a)
abs	( a   -- b )	b is abs(a)
log	( a   -- b )	b is log(a)
sqrt	( a   -- b )	b is sqrt(a)
floot   ( a   -- b ) 	b is floor(a)
min	( a b -- c ) 	c is min(a, b)
max	( a b -- c ) 	c is max(a, b)
>	( a b -- c )    c:boolean is a > b
<	( a b -- c )    c:boolean is a < b
>=	( a b -- c )    c:boolean is a <= b
<=	( a b -- c )    c:boolean is a >= b
```


## Strings

If Forth does not recognize a word it will consider it a string literal. The (macro) word " will treat the next word as a string literal. Strings can have embedded spaces in them prefaced with a backslash. Strings support the escape sequences \s and \n.

These are all valid strings:
```
Hello 
" Hello 
Hello\ world!
Line\sone\nLine\stwo\n
```

```
word	stack notation	explanation
"	( -- b ) 	macro; parses a word from the input buffer and puts it on the stack
strlen  ( a -- b ) 	b is length of a:string
```

## Stack ordering

Objects on the stack can be rearranged if necessary. Sometimes the order of tack elements must be reordered to accomadate a word.

```
word	stack notation		explanation

dup 	( a   -- a a   )	duplicates the top word
swap 	( a b -- b a   )	swaps the top two words
drop 	( a   --       )	pops the top element
nip  	( a b -- b     )    	drops the element below the top element
over 	( a b -- a b a ) 	duplicates the object under the top element
```

There is also a second stack. The stack notation is extended to read ( a ; c -- c ; a ). This means that the top elements from both stacks are swapped. The part before the semicolon is the main stack. The part after is the second stack.

```
word	stack notation		explanation

twirl	( a ; b -- b ; a )	swaps top element of both stack
raise 	( a ;   -- ; a )	moves element from main to second stack
lower	( ; a   -- a ; ) 	moves element from second to main stack
```

## Conditional

The if/then/else construct found in other languages is implemented like this:
```
<boolean> if <execute when true> then 
<boolean> if <execute when true> else <execute when false> then
```

The if consumes the boolean on the top of the stack.

## Looping

The while or for loop construct found in other languages is implemented like this:
```
do <executes and leaves a boolean on top of stack> while <executes once when true> repeat
```

While consumes the boolean on the top of the stack.

## Constants
```
word	stack notation	explanation
true	( -- a )	a:boolean is true
false	( -- a )	a:boolean is false
e	( -- a )	a:number is math constant e 
PI	( -- a )	a:number is math constant PI
null 	( -- a )	a:null is null value
```

## Lists

A list, implemented using Vector is constructed like this:
```
( elements... ) 
list!
```

The first form is a literal. Note that when the list is constructed it is reversed to match the way it is visually represented so when a list is made like ( 1 2 3 4 ) then 1 is the first element and 4 the last.

```
word	stack notation	explanation
```

## Maps

A map is a dictionary implemented using HashMap that maps keys to values. A map is created like this:
```
map!
```
```
word	stack notation	explanation
get 	( b a -- c ) 	get c:value from b:key in a:map
put 	( c b a -- )    puts b:key and c:value in a:map
```

## Set

A set is implemented using HashSet and can be constructed with a constructor or a literal like this:
```
(+ <elements> +)
set!
```

word	stack notation	explanation

## Collection words

Words defined on lists:
```
word	stack notation	explanation

push	( b a --   )	push a on top of b:list
pop	( a   -- b )	pops top element from a:list on top of stack as b:object
at 	( b a -- c ) 	get c:value at index b:number from a:list
set     ( c b a -- ) 	set value of index b:number in list:c to value a
```

Words defined on all collection types:

```
word	stack notation	explanation

size	( a -- b   )	b is size of a:collection
clear	( a --     ) 	removes all elements from a
set	( a b c -- )	add pair of b:key or b:index and a:value to dictionary c
add     ( b a -    ) 	add value b to a:collection
has     ( b a -- c ) 	b is true or false if b is in a:collection or not
del	( b a --   )	remove b:value from a:collection 
union   ( b a -- b )   	adds a:collection to b:collection
```
## Variables

A named variable is created like this:

```
<init value> var <name of variable>
```

This pops the top value from the stack and initializes the variable with this value. 

Setting the variable with a new value is done like this:

```
<new value> ! <name of variable>
```

Naming the variable will put it's current variable on the stack so the following will put the value 42 on the stack.
```
42 var life 
life 
```

```
word		stack notation	explanation
var <name> 	( a -- )	creates a named variable and initializes it with 'a'
! <name>	( a -- )	sets the variable named to value 'a'
<name>		( -- a )	puts value of variable <name> on stack as a:object
```

## Lambdas

You can group statements togetehr in a lambda as follows:
```
[ ... ] statements
```

You can call it with teh word 'call' as follows:
```
[ 1 2 + ] call
```


## Words for displaying words

```
word	stack notation		explanation

putnl   ( a       --   )	outputs element a followed by a newline to standard output
puts    ( a       --   ) 	outputs element a to standard output
fmt     ( ... a b -- c ) 	... is a list of elements of size a:number and b is a format string
		 		c is the result of formatting using String.format 
nl      (         --   ) 	outputs newline to standard output
space   ( 	  --   ) 	outputs a space to standard output
```

## General Words

Words that work on all types:

```
word	stack notation		explanation

==	( a b     -- c )	c is a == b
cmp    	( a b     -- c )	c is a.compareTo(b)
class?  ( a       -- b ) 	b is the class name of a:object
```
