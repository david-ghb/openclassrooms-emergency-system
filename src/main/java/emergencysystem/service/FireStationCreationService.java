package emergencysystem.service;

import emergencysystem.dao.FireStationRepository;
import emergencysystem.model.FireStation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FireStationCreationService {

    @Autowired
    private FireStationRepository fireStationRepository;

    public FireStation createFireStation(FireStation fireStation) {

        return fireStationRepository.save(fireStation);
    }

    public List<FireStation> createFireStations(List<FireStation> fireStations) {

        return fireStationRepository.saveAll(fireStations);
    }
}
