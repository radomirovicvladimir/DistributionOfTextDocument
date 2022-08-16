package rs.ac.bg.etf.kdp.nikola.thread;

import rs.ac.bg.etf.kdp.nikola.baffer.ServerBuffer;
import rs.ac.bg.etf.kdp.nikola.baffer.UpdateBuffer;
import rs.ac.bg.etf.kdp.nikola.data.DocumentData;

public class UpdateBufferWorkingThread extends Thread {

	private UpdateBuffer updateBuffer;
	private ServerBuffer buffer;
	private int updateCheckTime;

	public UpdateBufferWorkingThread(UpdateBuffer updateBuffer, ServerBuffer buffer, int updateCheckTime) {
		this.updateBuffer = updateBuffer;
		this.buffer = buffer;
		this.updateCheckTime = updateCheckTime;
	}

	//Run metod niti koja proverava na odredjeni vremenski period da li je doslod do
	//izmene nekod fajla ili neije doslo do izmene u slucaju da jeste doslo do izmene
	//taj podatak se clonira i ubacuje u update server
	@Override
	public void run() {
		try {
			while (true) {
				sleep(1000 * updateCheckTime);
				for (int i = 0; i < buffer.size(); i++) {
					DocumentData data = buffer.getData(i);
					if(data.isChanged()) {
						updateBuffer.addData(data.clone());
						data.setChanged(false);
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
