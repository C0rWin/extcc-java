
package:
	tar -C chaincode/src -cvzf chaincode/pkg/code.tar.gz .
	tar -C chaincode/pkg -cvzf extcc.tar.gz metadata.json code.tar.gz

