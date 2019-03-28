package application;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;

import java.util.PriorityQueue;
import java.util.Stack;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SampleController {

	String inputName;
	String outputPathC;
	String outputPathD;

	File inFile;
	File outFile;
	int buffer;
	BufferedInputStream b1;
	BufferedInputStream b2;
	BufferedOutputStream a1;
	BufferedOutputStream a2;
	ArrayList<Boolean>[] aCoBool;// 2
	String[] ar;
	int[] ut;

	int cc;

	int allChars = 256;

	int[] countArr;

	int numOfChars; // in the inFile

	String header;
	String statics;
	int n;
	String headerH;
	int bitCounter;
	int uniqueChars;
	ArrayList<Boolean> outBitStreem;
	ArrayList<Boolean> inBitStreem;

	@FXML
	TextArea staticsArea;

	@FXML
	TextField fileName;

	@FXML
	TextArea headerArea;

	@FXML
	TableView<Data> tableview;
	@FXML
	TableColumn<Data, String> c1;

	@FXML
	TableColumn<Data, String> c2;
	@FXML
	TableColumn<Data, Integer> c3;

	@FXML

	void readFile(ActionEvent event) {

		FileChooser file = new FileChooser();
		file.setTitle("Choose a file to De/COMPRESS");
		inFile = file.showOpenDialog(new Stage());

		//fileName.setText(inFile.getPath());
		inputName = inFile.getName();

		// System.out.println(fileName);
		// System.out.println(inputName);
		// System.out.println(outputPath);

	}

	@FXML
	void compressF(ActionEvent event) throws IOException {

		outputPathC = inFile.getPath().substring(0, inFile.getPath().lastIndexOf('.')) + ".huf";

		if (inputName.substring(inputName.indexOf(".") + 1).equals("huf")) {

			System.out.println("YOU CAN'T Comp a file with a .huf exe");
			System.exit(1);
		}

		buffer = 0;

		// read input file byte by byte ..Scanner
		b1 = new BufferedInputStream(new FileInputStream(inFile));
		// write the file after the compression process ...PrintWriter
		a1 = new BufferedOutputStream(new FileOutputStream(outputPathC));

		// initialize the char of repeat ;
		countArr = new int[allChars];

		numOfChars = 0;

		/**
		 * Chars Counting :-
		 */

		/// find the frequency of each character
		while (b1.available() != 0) {
			byte[] ar = new byte[1];

			b1.read(ar); // return the next 8bits

			countArr[ar[0] & 0xFF]++;// b bits --128 choice , bitwise and operation

			numOfChars++; // increase the total number of characters in the file
			if (countArr[ar[0] & 0xFF] == 1) {
				uniqueChars++;
			}

		}

		b1.close();

		/**
		 * for(int i = 0 ; i<countArr.length;i++) System.out.println((char)(i) + " " +
		 * countArr[i]);
		 * 
		 * 
		 **/

		/**
		 * What is going here ..
		 * 
		 * make the tree and the huff codes
		 */

		CharTree root = makeTree(countArr);

		writeString(inputName.substring(inputName.indexOf(".") + 1));
		writeInt(uniqueChars);
		writeInt(numOfChars);
		String x = printInorder(root); // x (the traversal the huff code )
		System.out.println(x);
		for (int k = 0; k < x.length(); k++) {

			if (x.charAt(k) == '1') {
				writeBit(true);
				cc++;
				k++;

				char q = x.charAt(k);
				cc += 8;

				byte b = (byte) q;
				String aaa = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');

				// System.out.println(aaa);
				for (int y = 0; y < aaa.length(); y++) {
					if (aaa.charAt(y) == '1') {
						writeBit(true);
					} else if (aaa.charAt(y) == '0') {
						writeBit(false);

					}

				}
			} else if (x.charAt(k) == '0') {
				writeBit(false);
				cc++;

			} // else {

			// }
		}

		boolean c1 = checkBit(cc);
		// System.out.println(c1);
		// System.out.println(cc);

		/**
		 * construction of the huffman code ar:array of string...sized 256 s:huff code
		 */

		ar = new String[allChars];
		String s = "";

		// root >> counting arr ,ar >> array of huff codes ,, s:huffman code
		makeHuffCodes(root, ar, s);

		aCoBool = new ArrayList[allChars];
		stToBool(ar, aCoBool);
		header = ("File Extention:" + inputName.substring(inputName.indexOf(".") + 1) + "\n");
		header += ("Exisited chars -from the 256 chars in this file=" + uniqueChars + "\n");
		header += ("All chars # = " + numOfChars + "\n");
		header += ("Header key:" + x + "\n");

		// System.out.println(Math.ceil(h / 8));
		int headLength;
		if (c1) {

			headLength = (((cc + (8 - cc % 8)) / 8) + 8 + inputName.substring(inputName.indexOf(".") + 1).length());

		} else {

			headLength = (((cc) / 8) + 8 + inputName.substring(inputName.indexOf(".") + 1).length());
		}

		outBitStreem = new ArrayList<Boolean>();

		/// writing the content of the file by huff codes
		b1 = new BufferedInputStream(new FileInputStream(inFile));
		while (b1.available() != 0) {
			byte[] nxt = new byte[1];
			b1.read(nxt);
			for (int i = 0; i < aCoBool[(nxt[0] & 0xFF)].size(); i++) {
				outBitStreem.add(aCoBool[(nxt[0] & 0xFF)].get(i));
			}
		}

		for (int i = 0; i < outBitStreem.size(); i++) {
			writeBit(outBitStreem.get(i));

		}

		boolean v = checkBit(outBitStreem.size());
		int outDataSize;
		double ra;
		if (v) {
			outDataSize = (outBitStreem.size() + (8 - outBitStreem.size() % 8)) / 8;

			ra = (headLength + outDataSize) * 100 / numOfChars;
			statics = "header length: " + headLength + "(byte) \n out data size:" + outDataSize + "(byte)\n ";
			statics += " compress ratio " + ra + "%  \n";
		} else {
			outDataSize = (outBitStreem.size()) / 8;

			ra = (headLength + outDataSize) * 100 / numOfChars;
			statics = "header length: " + headLength + "(byte) \n out data size:" + outDataSize + "(byte)\n ";
			statics += " compress ratio " + ra + "%  \n";

		}

		// System.out.println(outBitStreem.size());

		a1.flush();
		b1.close();
		a1.close();
	}

	// check bit to make it 8*x -x is an int -

	private boolean checkBit(int cc2) throws IOException {

		if (cc2 % 8 == 0) {
			return false;
		} else {
			int j = (8 - (cc2 % 8));
			// System.out.println(j);
			for (int k = j; k > 0; k--) {
				writeBit(false);
				// System.out.println("hello");
			}
			return true;
		}

	}

	// Preorder traversal ..
	String printInorder(CharTree node) throws IOException {

		Stack<CharTree> stk = new Stack<>();
		stk.push(node);
		headerH = "";

		CharTree temp;
		while (!stk.isEmpty()) {
			temp = stk.pop();
			if (temp.isLeaf()) {

				headerH += "1";
				headerH += temp.getC();

			} else {

				headerH += "0";
			}

			if (temp.getRight() != null) {
				stk.push(temp.getRight());
			}
			// if code has left, then push to the stack
			if (temp.getLeft() != null) {
				stk.push(temp.getLeft());
			}
		}

		return headerH;
	}

	@FXML
	void writeStatics(ActionEvent event) {
		staticsArea.appendText("File name: " + inputName + "\n");
		// headerArea.appendText(inputName + "\n");
		staticsArea.appendText("InFile size: " + numOfChars + " (byte)\n");

		staticsArea.appendText(statics);
		c1.setCellValueFactory(new PropertyValueFactory<Data, String>("c1"));
		c3.setCellValueFactory(new PropertyValueFactory<Data, Integer>("c3"));
		c2.setCellValueFactory(new PropertyValueFactory<Data, String>("c2"));
		ObservableList<Data> data = FXCollections.observableArrayList();

		for (int i = 0; i < allChars; i++) {
			if (countArr[i] != 0)

				data.add(new Data(Character.toString(((char) (i & 0xFF))), countArr[i], ar[i]));
		}

		tableview.setItems(data);
	}

	@FXML
	void writeHeader(ActionEvent event) {

		staticsArea.appendText(header);
	}

	/**
	 * huff code by recursion:
	 * 
	 * @param root
	 * @param ar
	 * @param s
	 */

	private void makeHuffCodes(CharTree root, String[] ar, String s) {

		if (!root.isLeaf()) {

			if (root.left != null) {
				makeHuffCodes(root.left, ar, s + '0');
			}
			makeHuffCodes(root.right, ar, s + '1');

		} else {
			ar[root.getC()] = s;
		}

	}

	private CharTree makeTree(int[] countArr2) {

		PriorityQueue<CharTree> pq = new PriorityQueue<CharTree>();
		for (char i = 0; i < allChars; i++) {
			if (countArr2[i] > 0)
				pq.offer(new CharTree(i, countArr2[i]));
			// construct group of nodes
		}

		if (pq.size() == 1) {

			// if there is only one char repeating in the file
			CharTree right = pq.poll();

			CharTree parent = new CharTree('\0', right.getFreq(), new CharTree('\0'), right);
			pq.offer(parent);

		}

		// construct the tree using the quque -- >
		/**
		 * pull the 2 mins and merge them ... repeat
		 */
		while (pq.size() > 1) {

			CharTree left = pq.poll();
			CharTree right = pq.poll();
			
			CharTree parent = new CharTree('\0', left.getFreq() + right.getFreq(), left, right);

			pq.offer(parent);

		}

		return pq.poll(); // return the root ;
	}

	public static void stToBool(String[] coSt, ArrayList<Boolean>[] aCoBool) {
		for (int i = 0; i < coSt.length; i++) {
			if (coSt[i] != null) {
				aCoBool[i] = new ArrayList<Boolean>();
				char[] ch = coSt[i].toCharArray();
				for (int j = 0; j < ch.length; j++) {
					if (ch[j] == '1')
						aCoBool[i].add(true);
					else
						aCoBool[i].add(false);
				}
			}
		}
	}

	@FXML
	void deComp(ActionEvent event) throws IOException {

		b2 = new BufferedInputStream(new FileInputStream(inFile));

		if (!inputName.substring(inputName.indexOf(".") + 1).equals("huf")) {

			System.out.println("YOU CAN'T Deco a file with not .huf exe");
			System.exit(1);
		}

		outputPathD = inFile.getPath().substring(0, inFile.getPath().lastIndexOf('.')) + ".";
		byte[] bytes = new byte[4];

		byte[] finalDD = new byte[1];
		boolean hSi = true;
		int qq = 0;
		CharTree ratoie = null;
		int bitInH = 0;
		int bytoe = 0;
		boolean x = false;
		boolean headaro = true;
		boolean path = true;
		boolean noc = true;
		String hekoda = "";
		int numOfOriginalChar = 0;
		String kayto = "";
		String kayto2 = "";

		int numberOfCharaters = 0;

		while (b2.available() != 0) {

			if (path) {
				outputPathD += (char) b2.read();
				outputPathD += (char) b2.read();
				outputPathD += (char) b2.read();
				a2 = new BufferedOutputStream(new FileOutputStream(outputPathD));

				System.out.println("step1>>>>>>>" + outputPathD);
				path = false;
			}

			if (hSi) {

				b2.read(bytes);
				int e = readInt(bytes);
				qq = e;

				bitInH = qq * 10 - 1;

				System.out.println("step2>>>>>>>>>>>" + bitInH); // 29

				x = findClosestByte(bitInH);
				if (x) {
					bytoe = (bitInH + (8 - bitInH % 8));
				} else {
					bytoe = bitInH;

				}

				hSi = false;

			}

			if (noc) {

				b2.read(bytes);
				numberOfCharaters = readInt(bytes);

				System.out.println("step3>>>>>>>>>>" + numberOfCharaters);

				noc = false;

			}

			else {

				if (headaro) {
					byte[] ae = new byte[1];

					for (int i = 0; i < bytoe / 8; i++) {

						b2.read(ae);

						hekoda += String.format("%8s", Integer.toBinaryString(ae[0] & 0xFF)).replace(' ', '0');
						// System.out.println(i + " : " + hekoda);
						
					}

					System.out.println("step4>>>>>>>>>>>>>>>>>>>>" + hekoda);

					numOfOriginalChar = hekoda.length() - bitInH;

					System.out.println("fake=" + numOfOriginalChar);
					System.out.println("hekoda" + hekoda);//
					ratoie = reBuildTree(hekoda, numOfOriginalChar);

					headaro = false;

				}

				b2.read(finalDD);

				kayto += String.format("%8s", Integer.toBinaryString(finalDD[0] & 0xFF)).replace(' ', '0');

				kayto2 = kayto;
				CharTree cu = ratoie;

				for (int j = 0; j < kayto.length() && numberOfCharaters != 0; j++) {

					if (kayto.charAt(j) == '0') {

						cu = cu.getLeft();
					} else {
						cu = cu.getRight();

					}

					// reached leaf node
					if (cu.left == null && cu.right == null) {

						a2.write(cu.getC());
						numberOfCharaters--;

						if (j + 1 <= kayto.length()) {
							kayto2 = kayto.substring(j + 1);
						}
						cu = ratoie;

					}

				}

				if (kayto2 != null) {
					kayto = kayto2;

				}
			}

		}

		a2.flush();
		b2.close();
		a2.close();

	}

	private CharTree reBuildTree(String hekoda, int x) throws IOException {

		CharTree root = new CharTree('\0');

		String hekoda2 = "";

		for (int i = 0; i < hekoda.length() - x;) {

			if (hekoda.charAt(i) == '0') {
				hekoda2 += "0";
				i++;

			} else {

				hekoda2 += "1";

				i++;

				if (i + 8 <= hekoda.length() - x) {
					int cc = Integer.parseInt(hekoda.substring(i, i + 8), 2);

					hekoda2 += (char) (cc & 0xFF);

					i = i + 8;
				}

			}

		}

		Stack<CharTree> stack = new Stack<>();
		stack.push(root);

		System.out.println(hekoda2 + "hekoda2 ");

		for (int i = 0; i < hekoda2.length(); i++) {
			CharTree curr = new CharTree('\0');

			// pop the stack and put in current
			if (!stack.isEmpty()) {
				curr = stack.pop();
			}

			// if bit is 0 then build a subtree and push right and left of it to stack
			if (hekoda2.charAt(i) == '0') {
				curr.setRight(new CharTree('\0'));

				curr.setLeft(new CharTree('\0'));

				stack.push(curr.getRight());

				stack.push(curr.getLeft());
			} else {
				if (i + 1 <= hekoda2.length())
					curr.setC(hekoda2.charAt(i + 1));
				i++;

			}
		}

		// put the root as class global huffman tree root

		// inOrder(root) ;
		return root;

	}

	protected int readInt(byte[] bytes) {
		if (bytes != null) {
			int value = 0;
			value += (bytes[3] & 0x000000FF) << 24;
			value += (bytes[2] & 0x000000FF) << 16;
			value += (bytes[1] & 0x000000FF) << 8;
			value += (bytes[0] & 0x000000FF);
			return value;
		}
		return 0;
	}

	private boolean findClosestByte(int bitInH) {

		if (bitInH % 8 == 0)
			return false;
		else {
			return true;
		}

	}

	// System.out.println(outputPathD);

	public void writeString(String str) throws IOException {
		for (int i = 0; i < str.length(); i++)
			a1.write((byte) str.charAt(i));

	}

	public void writeInt(int ii) throws IOException {
		for (int i = 0; i < 4; i++) { // L>8
			a1.write((byte) ii);
			ii >>= 8;
		}
	}

	public void writeByte(byte b) throws IOException {
		a1.write(b);
	}

	public void writeBit(boolean bit) throws IOException {
		// add bit to buffer
		buffer <<= 1;
		if (bit)
			buffer |= 1;

		// if buffer is full (8 bits), write out as a single byte
		n++;
		if (n == 8) {
			a1.write((byte) buffer);
			buffer = 0;
			n = 0;
		}
	}

	void inOrder(CharTree node) {
		if (node == null) {
			return;
		}

		System.out.println(node.getC() + "ff");

		inOrder(node.left);
		inOrder(node.right);
	}

}
