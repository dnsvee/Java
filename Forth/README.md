# Forth interpreter

Attempt to create a Forth interpreter in Java. Some things work. 

Forth is a programming language with very little syntax and simple semantics. Where other languages have functions or methods Forth has words. Instead of function paramaters and return values Forth words reading from and write to globally available FIFO stack. Every word uses the same stack for interoperatibility. 

Running a Forth command looks like this:
```
1 2 + puts
```

The first two numbers are put on the stack and then it runs the '+' command. This command pops both numbers from the stack and adds them together; the sum is placed back on top of the stack. The 'puts' word outputs the value to standard output. 

To describe for each word what type of elements it consumes from the stack and what values it pushes back on the stack, and in what order, a visual representation called stack notation will be used. So for the word '+' which sums numbers the following stack notation is used: ( b a -- c ) where a, b are numbers and c is the result of adding a and b. The part before the dashes is the state of the stack before calling the word where 'a' is top of the stack and 'b' is below it. The part after the dashes is the description of the top of the stack after the word has executed. 

This interpreter also has available a second stack for use.

The available types in this language are double, string, boolean, list, map, set, null, pair. And are almost identical to their Java counterparts.

## Numbers

Numbers are double values and can be written using the syntax supported by the Double.valueOf operator described in the Java reference.

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
```

## Strings

If Forth does not recognize a word it will consider it a string literal. The word " will treat the next word as a string literal. Strings also can have embedded spaces in them prefaced with a backslash. Strings support the escape sequences \s and \n.

These are all valid strings:
```
Hello 
" Hello 
Hello\ world!
Line\sone\nLine\stwo\n
```

Any object can be converted to a string using 'str!'.

```
word	stack notation	explanation
"	( -- b ) 	parses word following it and treats it as a string literal
strlen  ( a -- b ) 	b is length of a:string
str!   	( a -- b )	b is a.toString()
```

## Stack ordering

Objects on the stack can be rearranged if necessary. Sometimes the order of stack elements must be reordered to accommodate a word.

```
word	stack notation		explanation

dup 	( a   -- a a   )	duplicates the top word
swap 	( b a -- a b   )	swaps the top two words
drop 	( a   --       )	pops the top element
nip  	( b a -- a     )    	drops the element below the top element
over 	( b a -- b a b ) 	duplicates the object under the top element
```

There is also a second stack. The stack notation is extended to read ( a ; c -- c ; a ). This means that the top elements from both stacks are swapped. The part before the semicolon is the main stack. The part after is the second stack.
```
word	stack notation		explanation

flip    ( a ; b -- b ; a )	swaps top element of both stack
raise 	( a ;   -- ; a )	moves element from main to second stack
lower	( ; a   -- a ; ) 	moves element from second to main stack
```

## Conditional

The if/then/else construct found in other languages is implemented like this:
```
<boolean> if <execute when true> then 
<boolean> if <execute when true> else <execute when false> then
```

The `if` consumes the boolean on the top of the stack.

## Looping

The while or for loop construct found in other languages is implemented like this:
```
do <statements that should leave a boolean on top of stack> while <statements that execute once> repeat
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

A list, implemented using Java's Stack class is constructed like this:
```
( elements... ) 
list!
```

The first form is a literal. Note that when the list is constructed using literal notation it is reversed to match the way it is visually represented so when a list is made like ( 1 2 3 4 ) then `1` is the first element and `4` the last.

Words defined on lists:
```
word	stack notation	explanation

apush	( b a --   )	push b on top of a:list
apop	( a   -- b )	pops top element from a:list on top of stack as b:object
aget 	( b a -- c ) 	get c:value at index b:number from a:list
aset    ( c b a -- ) 	set value of index b:number in a:list to value c
aconcat ( b a -- a ) 	adds values of b:list to a:list 
aclear  ( a --     )    clears a:list
asize   ( a -- b   ) 	b:number is size of a:list
```

## Maps

A map is a dictionary implemented using HashMap that maps keys to values. A map is created like this:
```
map!
```

```
word	stack notation	explanation

mget 	( b a -- c ) 	get c:value from b:key in a:map
mget* 	( c b a -- d ) 	if b:key in a:map than d is value associated with b:key, otherwise d is the same as c
mput 	( c b a -- )    puts b:key and c:value in a:map
mclear  ( a --     )    clears a:map
msize   ( a -- b   )    b:number is size of a:map
mdel    ( b a --   )    removes key and value with b:key from a:map
```

## Pairs

A pair, a collection of two objects, is created in two ways
```
1 one pair!
2 two ,
```

Pairs can be compared. The comparison is done by comparing the first element of both pairs and than the second element. Pairs with the same value elements are considered equal. Pairs can be used as keys in maps.

## Set

A set is implemented using Java's HashSet class and can be constructed with a constructor or a literal like this:
```
(+ <elements> +)
set!
```

```
word	stack notation	explanation
sadd	( b a --   )    adds a to b:set
shas	( b a -- c )    c is true or false if b is in a:set or not
sdel    ( b a --   )	removes b from a:set
sunion  ( b a -- b )    adds values of b:set to a:set
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

Naming the variable will put it's current value on the stack so the following will put the value 42 on the stack.
```
42 var life 
life 
```

Multiple variables with the same name can exist. The more recently defined variable shadows the older one.

```
word		stack notation	explanation
var <name> 	( a -- )	creates a named variable and initializes it with 'a'
! <name>	( a -- )	sets the variable named to value 'a'
<name>		( -- a )	puts value of variable <name> on stack as a:object
```

## Lambdas

You can group statements together in a lambda as follows:
```
[ ... ] statements
```

You can call it with the word 'call' as follows:
```
[ 1 2 + ] call
```

## IO words

```
word	stack notation		explanation

putnl   ( a       --   )	outputs a followed by a newline to standard output
puts    ( a       --   ) 	outputs a to standard output
fmt     ( ... a b -- c ) 	... is a list of elements of size a:number and b is a format string
		 		c is the result of formatting using String.format 
nl      (         --   ) 	outputs newline to standard output
space   ( 	  --   ) 	outputs a space to standard output
```

## User defined words

A named function is created as follows:
```
def <name>
	<statements>
end
```

Multiple words can have the same name. The most recently defined word shadows the older word. Words are never overwritten so when a new word is created with the same name as an older one the references to the old word still point to the previous definition.

Example:
```
def anumber
	4
end

def puts_number
	anumber puts
end

def anumber 
	5
end
```

The word puts_number will output 4.

## General Words

Words that work on all types:

```
word	stack notation		explanation

class?  ( a       -- b ) 	b is the class name of a:object
==	( a b     -- c )	c is a.equals(b)
>	( a b -- c )    	c:boolean is a > b
<	( a b -- c )    	c:boolean is a < b
>=	( a b -- c )    	c:boolean is a <= b
<=	( a b -- c )   	 	c:boolean is a >= b
```

## Files in repo

Tester.java runs a special test program on the input file. It will run each line in file separately and if it finds a value on the stack that's not true it will treat this line as a test failure. tests.fth contains some test cases

Interp.java is the interactive interpreter for the Forth engine. Each input line will be executed. An empty input line will exit the program.

RunScript.java runs the file provided as the first argument.
