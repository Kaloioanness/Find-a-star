package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.jsons.stars.StarsSeedDTO;
import softuni.exam.models.dto.xmls.AstronomerDTO;
import softuni.exam.models.dto.xmls.AstronomerRootDTO;
import softuni.exam.models.entity.Constellation;
import softuni.exam.models.entity.Star;
import softuni.exam.models.entity.StarType;
import softuni.exam.repository.ConstellationRepository;
import softuni.exam.repository.StarRepository;
import softuni.exam.service.StarService;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;


@Service
public class StarServiceImpl implements StarService {

    private static final String FILE_PATH = "src/main/resources/files/json/stars.json";

    private final StarRepository starRepository;
    private final ConstellationRepository constellationRepository;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    public StarServiceImpl(StarRepository starRepository, ConstellationRepository constellationRepository, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.starRepository = starRepository;
        this.constellationRepository = constellationRepository;

        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public boolean areImported() {
        return this.starRepository.count() > 0;
    }

    @Override
    public String readStarsFileContent() throws IOException {
        return new String(Files.readAllBytes(Path.of(FILE_PATH)));
    }

    @Override
    public String importStars() throws IOException {
        StringBuilder sb = new StringBuilder();
        StarsSeedDTO[] starsSeedDTOS = this.gson.fromJson(readStarsFileContent(), StarsSeedDTO[].class);
        for (StarsSeedDTO starsSeedDTO : starsSeedDTOS) {
            Optional<Star> optionalConstellation = this.starRepository.findByName(starsSeedDTO.getName());
            if (!validationUtil.isValid(starsSeedDTO) || optionalConstellation.isPresent()){
                sb.append("Invalid star\n");
                continue;
            }

            Star star = modelMapper.map(starsSeedDTO, Star.class);
            star.setStarType(StarType.valueOf(starsSeedDTO.getStarType()));
            star.setConstellation(this.constellationRepository.getById(starsSeedDTO.getConstellation()));
            this.starRepository.saveAndFlush(star);

            DecimalFormat df = new DecimalFormat("0.00");
            df.setDecimalSeparatorAlwaysShown(true);                                        // замества с точка
            df.setDecimalFormatSymbols(new DecimalFormatSymbols(java.util.Locale.US));

            sb.append(format("Successfully imported star %s - %s light years\n",star.getName(),df.format(star.getLightYears())));
        }
        return sb.toString();
    }

    @Override
    public String exportStars(){
        DecimalFormat df = new DecimalFormat("0.00");
        df.setDecimalSeparatorAlwaysShown(true);                                        // замества с точка
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(java.util.Locale.US));

       return this.starRepository.getStarByStarTypeOrderedByLightYears()
                .stream()
               .map(star -> format("Star: %s\n" +
                               "   *Distance: %s light years\n" +
                               "   **Description: %s\n" +
                               "   ***Constellation: %s\n"
                            ,star.getName(),df.format(star.getLightYears()),star.getDescription(),star.getConstellation().getName()))
               .collect(Collectors.joining());



    }


}
