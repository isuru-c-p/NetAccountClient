javac -g:none NetLogin.java
jar cf JNetLogin.jar *.class nz joptsimple
del -r *.class
echo  "\nUsage: java -cp JNetLogin.jar NetLogin\n"