SOURCE = NetLogin.java \
		NetLoginPreferences.java \
		NetLoginConnection.java \
		PingRespHandler.java \
		PingSender.java \
		ReadResult.java \
		PasswordChanger.java \
		SPP_Packet.java

all: JNetLogin.jar

JNetLogin.jar: $(SOURCE)
	javac -g:none $(SOURCE)
	jar cf JNetLogin.jar *.class nz
	rm *.class
	echo -e "\nUsage: java -cp JNetLogin.jar NetLogin\n"

clean:
	rm JNetLogin.jar
