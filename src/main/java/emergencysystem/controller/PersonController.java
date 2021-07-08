package emergencysystem.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import emergencysystem.model.FireStation;
import emergencysystem.model.MedicalRecord;
import emergencysystem.model.Person;
import emergencysystem.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class PersonController {

    @Autowired
    private PersonCreationService personCreationService;
    @Autowired
    private PersonReadService personReadService;
    @Autowired
    private PersonUpdateService personUpdateService;
    @Autowired
    private PersonDeletionService personDeletionService;

    @Autowired
    private FireStationCreationService fireStationCreationService;

    @Autowired
    private MedicalRecordCreationService medicalRecordCreationService;
    @Autowired
    private MedicalRecordReadService medicalRecordReadService;

    private static final Logger logger = LogManager.getLogger(PersonReadService.class);

    @GetMapping("/persons")
    public List<Person> getPersons() throws IOException {

        return personReadService.getPersons();
    }

    @GetMapping("/persons/{id}")
    public Person getPersonById(@PathVariable Long id) {

        return personReadService.getPersonById(id);
    }

    @PostMapping("/createPerson")
    public Person createPerson(@RequestBody Person person) {

        return personCreationService.createPerson(person);
    }

    @PostMapping("/createPersons")
    public List<Person> createPersons(@RequestBody List<Person> persons) {

        return personCreationService.createPersons(persons);
    }

    @PutMapping("/updatePerson")
    public Person updatePerson(@RequestBody Person person) {

        return personUpdateService.updatePerson(person);
    }

    @DeleteMapping("/persons/{id}")
    public String deletePersonById(@PathVariable Long id) {

        return personDeletionService.deletePersonById(id);
    }

    @GetMapping("/flood/stations")
    public Map<String, List<Map<String, String>>> getPersonsByStations(@RequestParam List<Integer> stations) {

        return personReadService.getPersonsByStations(stations);
    }

    @GetMapping("/personInfo")
    public List<Map<String, String>> getPersonsByFirstNameAndLastName(@RequestParam String firstName, String lastName) {

        logger.debug("[PERSONINFO] " + firstName + " " + lastName);

        return personReadService.getPersonsByFirstNameAndLastName(firstName, lastName);
    }

    @GetMapping("/communityEmail")
    public Set<String> getEmailsByCity(@RequestParam String city) {

        return personReadService.getEmailsByCity(city);
    }

    @GetMapping("/init")
    public String initializeData(@RequestParam String fileName) throws IOException {

        String file = "src/main/resources/" + fileName;

        String json = new String(Files.readAllBytes(Paths.get(file)));
        JsonNode jsonNode = JsonService.parse(json);

        List<Person> persons = new ArrayList<>();
        List<FireStation> fireStations = new ArrayList<>();
        List<MedicalRecord> medicalRecords = new ArrayList<>();

        jsonNode.get("persons").forEach(p -> {
            Person person = new Person();
            try {
                person = JsonService.fromJson(p, Person.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            persons.add(person);
        });

        jsonNode.get("fireStations").forEach(f -> {
            FireStation fireStation = new FireStation();
            try {
                fireStation = JsonService.fromJson(f, FireStation.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            fireStations.add(fireStation);
        });

        jsonNode.get("medicalRecords").forEach(m -> {
            ObjectNode o = (ObjectNode) m;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            o.put("birthDate", String.valueOf(LocalDate.parse(m.get("birthDate").asText(), formatter)));
            MedicalRecord medicalRecord = new MedicalRecord();
            try {
                medicalRecord = JsonService.fromJson(o, MedicalRecord.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            medicalRecords.add(medicalRecord);
        });

        personCreationService.createPersons(persons);
        fireStationCreationService.createFireStations(fireStations);
        medicalRecordCreationService.createMedicalRecords(medicalRecords);

        return Paths.get(file).getFileName() + " was successfully initialized!";
    }
}
