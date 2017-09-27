import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class TamperEvidentLogTest {
	
	@Test
	public void testAddMsg() throws IOException {
			TamperEvidentLog tel = new TamperEvidentLog();
			tel.addItem("msg1");
			assertEquals(1, tel.getItemCount());
			tel.addItem("msg2");
			assertEquals(2, tel.getItemCount());
			tel.addItem("msg3");
			assertEquals(3, tel.getItemCount());
			tel.addItem("msg4");
			assertEquals(4, tel.getItemCount());
			tel.dumpItems();
	}

	@Test
	public void testFakeMsg() {
		TamperEvidentLog tel = new TamperEvidentLog();
		try {
			tel.addItem("msg1");
			tel.addItem("msg2");
			tel.addItem("msg3");
			tel.addItem("msg4");
			boolean ret = tel.checkIntegrity();
			assertTrue(ret);
			assertEquals(4, tel.getItemCount());
			TamperEvidentLog.lastItem.item  = "fake msg";
			ret = tel.checkIntegrity();
			assertFalse(ret);
			assertEquals(4, tel.getItemCount());
			tel.dumpItems();
			TamperEvidentLog.lastItem.item  = "msg4";
			ret = tel.checkIntegrity();
			assertTrue(ret);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFakeMsgLnk() {
		TamperEvidentLog tel = new TamperEvidentLog();
		try {
			tel.addItem("msg1");
			tel.addItem("msg2");
			tel.addItem("msg3");
			tel.addItem("msg4");
			boolean ret = tel.checkIntegrity();
			assertTrue(ret);
			assertEquals(4, tel.getItemCount());
			TamperEvidentLog.lastItem.prevItem = TamperEvidentLog.lastItem.prevItem.prevItem;
			ret = tel.checkIntegrity();
			assertFalse(ret);
			assertEquals(4, tel.getItemCount());
			
			tel.dumpItems();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
