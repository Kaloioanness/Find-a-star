package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.xmls.AstronomerDTO;
import softuni.exam.models.dto.xmls.AstronomerRootDTO;
import softuni.exam.models.entity.Astronomer;
import softuni.exam.models.entity.Star;
import softuni.exam.repository.AstronomerRepository;
import softuni.exam.repository.StarRepository;
import softuni.exam.service.AstronomerService;
import softuni.exam.util.ValidationUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Optional;


@Service
public class AstronomerServiceImpl implements AstronomerService {

    private static final String FILE_PATH = "src/main/resources/files/xml/astronomers.xml";

    private final AstronomerRepository astronomerRepository;
    private final StarRepository starRepository;
    private final Gson gson;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;


    public AstronomerServiceImpl(AstronomerRepository astronomerRepository, StarRepository starRepository, Gson gson, ModelMapper modelMapper, ValidationUtil validationUtil) {
        this.astronomerRepository = astronomerRepository;
        this.starRepository = starRepository;
        this.gson = gson;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
    }

    @Override
    public boolean areImported() {
        return this.astronomerRepository.count() > 0;
    }

    @Override
    public String readAstronomersFromFile() throws IOException {
        return new String(Files.readAllBytes(Path.of(FILE_PATH)));
    }

    @Override
    public String importAstronomers() throws IOException, JAXBException {
        StringBuilder sb = new StringBuilder();

        JAXBContext jaxbContext = JAXBContext.newInstance(AstronomerRootDTO.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        AstronomerRootDTO unmarshal = (AstronomerRootDTO) unmarshaller.unmarshal(new File(FILE_PATH));

        for (AstronomerDTO astronomerDTO : unmarshal.getAstronomer()) {
            Optional<Astronomer> optionalAstronomer = this.astronomerRepository
                    .findByFirstNameAndLastName(astronomerDTO.getFirstName(), astronomerDTO.getLastName());
            Optional<Star> optionalStar = this.starRepository.findById(astronomerDTO.getStarId());

            if (!validationUtil.isValid(astronomerDTO) || optionalAstronomer.isPresent() || optionalStar.isEmpty()){
                sb.append("Invalid astronomer\n");
                continue;
            }
            Astronomer astronomer = this.modelMapper.map(astronomerDTO, Astronomer.class);
            astronomer.setObservingStar(optionalStar.get());
            this.astronomerRepository.saveAndFlush(astronomer);

            DecimalFormat df = new DecimalFormat("0.00");
            df.setDecimalSeparatorAlwaysShown(true);                                        // замества с точка
            df.setDecimalFormatSymbols(new DecimalFormatSymbols(java.util.Locale.US));

            sb.append(String.format("Successfully imported astronomer %s %s - %s\n",astronomer.getFirstName(),astronomer.getLastName(),
                    df.format(astronomer.getAverageObservationHours())));
        }

        return sb.toString();
    }
}
