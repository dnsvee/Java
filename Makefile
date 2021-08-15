TreapTest.class: TreapTest.java Treap/Treap.class
	javac TreapTest.java

Treap/Treap.class: Treap/Treap.java
	javac Treap/Treap.java

run: TreapTest.class
	java TreapTest
