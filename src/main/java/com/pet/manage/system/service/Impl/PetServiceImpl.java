package com.pet.manage.system.service.Impl;

import com.pet.manage.system.Utils;
import com.pet.manage.system.dtos.*;
import com.pet.manage.system.dtos.request.AppointmentRequestDTO;
import com.pet.manage.system.dtos.response.AppointmentResponseDTO;
import com.pet.manage.system.dtos.response.PetDashboardDTO;
import com.pet.manage.system.entity.*;
import com.pet.manage.system.global.exception.DuplicateAppointmentException;
import com.pet.manage.system.repository.*;
import com.pet.manage.system.service.HelperUtilService;
import com.pet.manage.system.service.PetService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PetServiceImpl implements PetService {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetVaccinationRecordRepository vaccinationRecordRepository;

    @Autowired
    private HelperUtilService helperUtilService;

    @Autowired
    private PetMedicalRepository petMedicalRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public OwnerResponseDto registerPet(PetRegistrationDto petRegistrationDto, MultipartFile petPhoto) throws IOException {

        Owner owner = helperUtilService.findOwnerByContact(petRegistrationDto.getOwnerContact());

        if (owner == null) {
            throw new RuntimeException("Owner not found with provided contact.");
        }
        Pet petEntity = modelMapper.map(petRegistrationDto, Pet.class);
        petEntity.setOwner(owner);
        // Save photo if present
        if (petPhoto != null && !petPhoto.isEmpty()) {
            petEntity.setPhoto(petPhoto.getBytes()); // You may want to handle IOException
            petEntity.setPhotoContentType(petPhoto.getContentType());
        }
        petRepository.save(petEntity);

        return Utils.getOwnerDetails(owner, modelMapper);
    }


    @Override
    public PetVaccinationRecorResponsedDTO saveVaccinationRecord(PetVaccinationRecorRequestdDTO petVaccinationRecorRequestdDTO) {

        Owner owner = helperUtilService.findOwnerByContact(petVaccinationRecorRequestdDTO.getOwnerContact());

        PetVaccinationRecord petVaccinationRecordEntity = modelMapper.map(petVaccinationRecorRequestdDTO, PetVaccinationRecord.class);
        petVaccinationRecordEntity.setId(null); // Ensure it's a new entity
        owner.getPets().stream()
                .filter(pet -> pet.getId().equals(petVaccinationRecorRequestdDTO.getPetId()))
                .findFirst()
                .ifPresentOrElse(
                        pet -> petVaccinationRecordEntity.setPet(pet),
                        () -> {
                            throw new RuntimeException("Pet not found with provided name for the owner.");
                        }
                );

        PetVaccinationRecord petVaccinationRecordSave = vaccinationRecordRepository.save(petVaccinationRecordEntity);

        return modelMapper.map(petVaccinationRecordSave, PetVaccinationRecorResponsedDTO.class);
    }

    @Override
    public void savePetMedicalRecord(PetMedicalRequestDto petMedicalRequestDto) {

    }

    @Override
    public PetMedicalRespnseDto savePetMedicalDetails(PetMedicalRequestDto petMedicalRequestDto) {
        Owner owner = helperUtilService.findOwnerByContact(petMedicalRequestDto.getOwnerContact());

        PetMedical petMedical = PetMedical.builder()
                .diagnosis(petMedicalRequestDto.getDiagnosis())
                .treatmentSuggestions(petMedicalRequestDto.getTreatmentSuggestions())
                .validateTill(petMedicalRequestDto.getValidateTill())
                .visitDate(LocalDate.now())
                .build();
        //redundant code make
        owner.getPets().stream()
                .filter(pet -> pet.getId().equals(petMedicalRequestDto.getPetId()))
                .map(pet -> {
                    pet.setAllergies(petMedicalRequestDto.getAllergies());
                    return pet;
                })
                .findFirst()
                .ifPresentOrElse(
                        petMedical::setPet,
                        () -> {
                            throw new RuntimeException("Pet not found with provided name for the owner.");
                        }
                );

        Type listType = new TypeToken<List<Prescription>>() {
        }.getType();
        List<Prescription> prescriptions = modelMapper.map(
                petMedicalRequestDto.getPrescriptions(), listType
        );
        prescriptions.forEach(prescription -> prescription.setPetMedical(petMedical));
        petMedical.setPrescriptions(prescriptions);

        PetMedical savedPetMedical = petMedicalRepository.save(petMedical);
        PetMedicalRespnseDto response = modelMapper.map(savedPetMedical, PetMedicalRespnseDto.class);
        response.setPetMedicalId(savedPetMedical.getPetMedicalId());
        response.setPetId(savedPetMedical.getPet().getId());
        response.setOwnerContact(savedPetMedical.getPet().getOwner().getPhoneNumber());
        return response;
    }

    @Override
    public AppointmentResponseDTO bookAppointment(AppointmentRequestDTO appointmentRequestDTO) {

        // Check if already booked for the same name and date
        Optional<Appointment> existing = appointmentRepository.findByEmailAndNameAndPhoneAndDate(
                appointmentRequestDTO.getEmail(),
                appointmentRequestDTO.getName(),
                appointmentRequestDTO.getPhone(),
                appointmentRequestDTO.getDate()
        );

        if (existing.isPresent()) {
            throw new DuplicateAppointmentException("You have already booked an appointment for this date.");
        }

        Appointment appointment = modelMapper.map(appointmentRequestDTO, Appointment.class);
        appointment.setStatus("waiting");

        Appointment saveAppointment = appointmentRepository.save(appointment);

        AppointmentResponseDTO appointmentResponseDTO = modelMapper.map(saveAppointment, AppointmentResponseDTO.class);

        return appointmentResponseDTO;
    }

    @Override
    public List<AppointmentResponseDTO> getAppointmentsByDate(String date) {
        if (date == null || date.isEmpty()) {
            date = LocalDate.now().toString();
        }
        List<Appointment> appointments = appointmentRepository.findByDate(date);

        List<AppointmentResponseDTO> dtos = appointments.stream()
                .map(appointment -> modelMapper.map(appointment, AppointmentResponseDTO.class))
                .collect(Collectors.toList());

        // Sort: waiting -> pending -> checked-in -> others, each by id ascending
        dtos.sort(Comparator
                .comparing((AppointmentResponseDTO a) -> {
                    String status = a.getStatus();
                    if ("waiting".equalsIgnoreCase(status)) return 0;
                    if ("pending".equalsIgnoreCase(status)) return 1;
                    if ("checked-in".equalsIgnoreCase(status)) return 2;
                    return 3;
                })
                .thenComparing(AppointmentResponseDTO::getId)
        );

        return dtos;
    }

    @Override
    public AppointmentResponseDTO updateStatus(Long id, String status, String action) {
        Appointment appt = appointmentRepository.findById(id).orElse(null);
        if (appt != null) {
            appt.setStatus(status);
            appt.setAction(action);
            appointmentRepository.save(appt);
        }

        return modelMapper.map(appt, AppointmentResponseDTO.class);
    }

    @Override
    public boolean isOwnerRegistered(String email) {
        return ownerRepository.existsByEmail(email);
    }

    @Override
    public PetDashboardDTO getDashboardData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        Owner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        // Get pets
        List<PetResponseDto> petDtos = owner.getPets().stream()
                .map(pet -> Utils.mapPetToResponseDto(pet, modelMapper))
                .collect(Collectors.toList());

        // Map owner to DTO using the same pet mapping used by dashboard pets
        OwnerResponseDto ownerDto = modelMapper.map(owner, OwnerResponseDto.class);
        ownerDto.setPets(petDtos);

        // Get pet ids
        List<Long> petIds = owner.getPets().stream()
                .map(Pet::getId)
                .collect(Collectors.toList());

        // Get vaccinations
        List<PetVaccinationRecord> vaccinationRecords = vaccinationRecordRepository.findByPetIdIn(petIds);
        List<PetVaccinationRecorResponsedDTO> vaccinationDtos = vaccinationRecords.stream()
                .map(record -> {
                    PetVaccinationRecorResponsedDTO dto = modelMapper.map(record, PetVaccinationRecorResponsedDTO.class);
                    dto.setOwnerContact(record.getPet().getOwner().getPhoneNumber());
                    return dto;
                })
                .collect(Collectors.toList());

        // Get medical records
        List<PetMedical> medicalRecords = petMedicalRepository.findByPetIdIn(petIds);
        List<PetMedicalRespnseDto> medicalDtos = medicalRecords.stream()
                .map(record -> {
                    PetMedicalRespnseDto dto = modelMapper.map(record, PetMedicalRespnseDto.class);
                    dto.setPetMedicalId(record.getPetMedicalId());
                    dto.setPetId(record.getPet().getId());
                    dto.setOwnerContact(record.getPet().getOwner().getPhoneNumber());
                    return dto;
                })
                .collect(Collectors.toList());

        // Get appointments
        List<Appointment> appointments = appointmentRepository.findByEmail(email);
        List<AppointmentResponseDTO> appointmentDtos = appointments.stream()
                .map(appointment -> modelMapper.map(appointment, AppointmentResponseDTO.class))
                .collect(Collectors.toList());

        return PetDashboardDTO.builder()
                .owner(ownerDto)
                .pets(petDtos)
                .vaccinations(vaccinationDtos)
                .medicalRecords(medicalDtos)
                .appointments(appointmentDtos)
                .build();
    }

    @Override
    public byte[] downloadPrescriptionPdf(Long petId, Long petMedicalId) {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Valid petId is required");
        }
        if (petMedicalId == null || petMedicalId <= 0) {
            throw new IllegalArgumentException("Valid petMedicalId is required");
        }

        PetMedical record = petMedicalRepository.findForPdfByPetIdAndMedicalId(petId, petMedicalId)
                .orElseThrow(() -> new RuntimeException("Medical record not found for provided petId and petMedicalId"));
        return buildPrescriptionPdf(record);
    }

    private byte[] buildPrescriptionPdf(PetMedical record) {
        Pet pet = record.getPet();
        Owner owner = pet != null ? pet.getOwner() : null;

        List<String> lines = new ArrayList<>();
        lines.add("Pet Medical Prescription");
        lines.add("");
        lines.add("Diagnosis: " + safe(record.getDiagnosis()));
        lines.add("Visit Date: " + safe(record.getVisitDate()));
        lines.add("Valid Till: " + safe(record.getValidateTill()));
        lines.add("Pet Name: " + safe(pet != null ? pet.getPetName() : null));
        lines.add("Owner Name: " + safe(owner != null ? owner.getOwnerName() : null));
        lines.add("Owner Contact: " + safe(owner != null ? owner.getPhoneNumber() : null));
        lines.add("Allergies: " + safe(record.getAllergies()));
        lines.add("Treatment Suggestions: " + safe(record.getTreatmentSuggestions()));
        lines.add("");
        lines.add("Prescriptions:");

        int index = 1;
        for (Prescription prescription : record.getPrescriptions()) {
            lines.add(index + ". Medicine: " + safe(prescription.getMedicine()));
            lines.add("   Dosage: " + safe(prescription.getDosage())
                    + " | Frequency: " + safe(prescription.getFrequency())
                    + " | Duration: " + safe(prescription.getDuration()));
            lines.add("   Timing(M/A/E/N): " + safe(prescription.getMorning()) + "/"
                    + safe(prescription.getAfternoon()) + "/" + safe(prescription.getEvening()) + "/"
                    + safe(prescription.getNight()) + " | Meal: " + safe(prescription.getMeal()));
            lines.add("   Instructions: " + safe(prescription.getInstructions()));
            lines.add("");
            index++;
        }

        return createSinglePagePdf(lines);
    }

    private byte[] createSinglePagePdf(List<String> lines) {
        try {
            StringBuilder textStream = new StringBuilder();
            textStream.append("BT\n");
            textStream.append("/F1 11 Tf\n");
            textStream.append("50 800 Td\n");
            for (String line : lines) {
                textStream.append("(").append(escapePdfText(line)).append(") Tj\n");
                textStream.append("0 -14 Td\n");
            }
            textStream.append("ET\n");

            byte[] contentBytes = textStream.toString().getBytes(StandardCharsets.ISO_8859_1);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();

            writeBytes(output, "%PDF-1.4\n");

            offsets.add(output.size());
            writeBytes(output, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            offsets.add(output.size());
            writeBytes(output, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

            offsets.add(output.size());
            writeBytes(output, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] ");
            writeBytes(output, "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");

            offsets.add(output.size());
            writeBytes(output, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

            offsets.add(output.size());
            writeBytes(output, "5 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n");
            output.write(contentBytes);
            writeBytes(output, "\nendstream\nendobj\n");

            int xrefOffset = output.size();
            writeBytes(output, "xref\n0 6\n");
            writeBytes(output, "0000000000 65535 f \n");
            for (Integer offset : offsets) {
                writeBytes(output, String.format("%010d 00000 n \n", offset));
            }
            writeBytes(output, "trailer\n<< /Size 6 /Root 1 0 R >>\n");
            writeBytes(output, "startxref\n" + xrefOffset + "\n%%EOF");

            return output.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to generate prescription PDF", ex);
        }
    }

    private void writeBytes(ByteArrayOutputStream output, String text) {
        try {
            output.write(text.getBytes(StandardCharsets.ISO_8859_1));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String escapePdfText(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private String safe(Object value) {
        return value == null ? "N/A" : value.toString();
    }
}
