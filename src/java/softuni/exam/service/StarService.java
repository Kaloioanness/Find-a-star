package softuni.exam.service;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public interface StarService {

    boolean areImported();

    String readStarsFileContent() throws IOException;
	
	String importStars() throws IOException;

    String exportStars();
}
