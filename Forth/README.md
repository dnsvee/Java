# Forth interpreter

Attempt to create a Forth interpreter in Java. Works a little bit right now. Lots of functionality still missing.

Forth is a programming language with very little syntax and simple semantics. 

When running the ForthTest.class file you will see a command prompt like
>>>

Forth commands look like this:
1 2 + puts

This means 1 and 2 are put on a globally available stack. First the command '+' is run which pops both numbers from the stack and adds them together. The 'puts' command pops the result from the stack and displays it.

## Some commands and control structures

TOS means top-of-stack.

Adding two numbers
1 2 +
3

Substracting two numbers
1 2 -
-1

Multiplying two numbers
2 3 **
8

Dividing two numbers
8 2 /
4

Displaying the TOS
16 puts
16

Comparing numbers:
1 2 <
true

1 2 >
false

PI cos
1

10 log
2.30258.....

## Constants

true, false, e, PI

## Strings

If Forth does not recognize a word it will consider it a string that is put on the stack as is.

The following will put the string hello\ world on the stack and print it to standard outpu

hello\ world puts

## Condition 

This control struct will display smaller since 1 is smaller than 2.

1 2 < if smaller else larger then puts












