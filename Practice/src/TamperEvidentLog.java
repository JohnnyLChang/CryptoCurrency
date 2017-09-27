import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import com.google.common.hash.Hashing;

public class TamperEvidentLog implements Serializable {
	public void addItem(Object obj) throws IOException {
		if(obj == null)
			return;
		TamperEvidentLog tel = new TamperEvidentLog();
		itemCount++;
		tel.prevItem = lastItem;
		tel.prevHash = lastHash;
		tel.item = obj;
		tel.id = itemCount;
		lastHash = getHash(tel);
		lastItem = tel;
		
	}

	public void removeItem(TamperEvidentLog obj) {
		if(lastItem != null) {
			prevHash = lastItem.prevHash;
			lastItem = lastItem.prevItem;
		}
	}
	
	public int getItemCount() {
		return itemCount;
	}

	private byte[] getHash(Object obj) throws IOException {
		return Hashing.sha256().hashBytes(TamperEvidentLog.serialize(obj)).asBytes();
	}

	public boolean checkIntegrity() throws IOException {
		TamperEvidentLog it = TamperEvidentLog.lastItem;
		boolean ret = true;
		byte[] prevHash = lastHash;
		while(it != null) {
			byte[] hash = getHash(it);
			if(!Arrays.equals(prevHash, hash)) {
				ret = false;
			}
			prevHash = it.prevHash;
			it = it.prevItem;
		}
		return ret;
	}
	
	public boolean dumpItems() throws IOException {
		TamperEvidentLog it = TamperEvidentLog.lastItem;
		boolean ret = true;
		byte[] prevHash = lastHash;
		while(it != null) {
			byte[] hash = getHash(it);
			boolean chk = Arrays.equals(prevHash, hash);
			//System.out.printf("id:%d, item:%s, integrity: %b\n", it.id, it.item, chk);
			if(chk)
				ret = false;
			prevHash = it.prevHash;
			it = it.prevItem;
		}
		return ret;
	}

	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}

	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}

	static TamperEvidentLog lastItem;
	static byte[] lastHash;
	int itemCount;

	TamperEvidentLog prevItem;
	byte[] prevHash;
	Object item;
	int id;
}
