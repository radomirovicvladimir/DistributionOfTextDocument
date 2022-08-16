package rs.ac.bg.etf.kdp.nikola.data;

import java.io.Serializable;
import java.util.List;

public interface DocumentInterface<T> extends Serializable {

	public void setFile(T file);

	public T getFile();

}
