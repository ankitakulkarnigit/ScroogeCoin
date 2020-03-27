
README Project 1: ScroogeCoin

CPSC-459 
Team Member1.	Mohit Kumar Cwid :887598266 mohit_kumar@csu.ullerton.edu
Team Member2. 	Ankita Udaykumar Kulkarni Cwid:887871861 ankitak@csu.fullerton.edu
Team Member3. 	Anuj R. Dhoot Cwid: 887450005
Team Member4.   Ryan Vo Cwid:888904216 rvo7496@csu.fullerton.edu

We have also implemented extra credit work for the project.

How to execute your program. 

	Unzip folder and place in C:/ 


	Mehtod : Run the TxnHandler and MaxFeeTxnHandler Test files against the autograder jar file to autograde the project.

		a) Testing TxHandler.java: Enter the following commands in your terminal from the current working directory.

		javac -cp scroogeCoinGrader.jar;rsa.jar;algs4.jar;. TestTxHandler.java
		java -cp scroogeCoinGrader.jar;rsa.jar;algs4.jar;. TestTxHandler

		b) Testing MaxFeeTxHandler.java: Enter the following commands in your terminal from the current working directory.

		javac -cp scroogeCoinGrader.jar;rsa.jar;algs4.jar;. TestMaxFeeTxHandler.java
		java -cp scroogeCoinGrader.jar;rsa.jar;algs4.jar;. TestMaxFeeTxHandler

Project Details:

TxnHaldler.java ----Implemented the isValidTx() and handleTxs() functions for transactions.
MaxFeeTxHandler.java ---- Implement on basic functionality of TxHandler handles fee of the transaction(Sum input-Sum Output)
