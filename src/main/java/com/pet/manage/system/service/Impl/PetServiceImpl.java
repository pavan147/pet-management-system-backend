package com.pet.manage.system.service.Impl;

import com.pet.manage.system.Utils;
import com.pet.manage.system.dtos.*;
import com.pet.manage.system.dtos.request.AppointmentRequestDTO;
import com.pet.manage.system.dtos.response.AppointmentResponseDTO;
import com.pet.manage.system.dtos.response.PetDashboardDTO;
import com.pet.manage.system.entity.*;
import com.pet.manage.system.global.exception.DuplicateAppointmentException;
import com.pet.manage.system.repository.*;
import com.pet.manage.system.repository.PetMedicalChatThreadRepository;
import com.pet.manage.system.service.HelperUtilService;
import com.pet.manage.system.service.PetService;
import com.pet.manage.system.service.TelegramNotificationService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PetServiceImpl implements PetService {

    private static final String CHAT_STATUS_ACTIVE = "ACTIVE";
    private static final String CHAT_STATUS_CLOSED = "CLOSED";
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png");

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
    private PetMedicalChatMessageRepository petMedicalChatMessageRepository;

    @Autowired
    private PetMedicalChatImageRepository petMedicalChatImageRepository;

    @Autowired
    private PetMedicalChatThreadRepository petMedicalChatThreadRepository;

    @Autowired
    private TelegramNotificationService telegramNotificationService;

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
        ownerDto.setPhoneVerified(Boolean.TRUE.equals(owner.getPhoneVerified()));

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
        return helperUtilService.generatePrescriptionPdf(record);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalChatThreadResponseDto getMedicalChatThread(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found for id: " + petId));
        Owner actor = getCurrentOwner();
        enforceConversationAccess(pet, actor);

        List<MedicalChatMessageResponseDto> messages = petMedicalChatMessageRepository
                .findByPetIdOrderByCreatedAtAsc(petId)
                .stream()
                .map(this::mapMessage)
                .collect(Collectors.toList());

        return MedicalChatThreadResponseDto.builder()
                .petId(pet.getId())
                .petName(pet.getPetName())
                .ownerId(pet.getOwner().getId())
                .ownerName(pet.getOwner().getOwnerName())
                .assignedVetId(pet.getAssignedVet() != null ? pet.getAssignedVet().getId() : null)
                .assignedVetName(pet.getAssignedVet() != null ? pet.getAssignedVet().getOwnerName() : null)
                .chatStatus(resolveChatStatus(pet))
                .closedAt(pet.getMedicalChatClosedAt())
                .closedByName(pet.getMedicalChatClosedByName())
                .canClose(canCloseChat(pet, actor))
                .canReply(canReplyToChat(pet, actor))
                .petContext(mapPetContext(pet))
                .vaccinationRecords(getVaccinationRecordsForPet(petId, false))
                .dewormingRecords(getVaccinationRecordsForPet(petId, true))
                .medicalRecords(getMedicalRecordsForPet(petId))
                .messages(messages)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalChatPetSearchResponseDto> searchMedicalChatPets(String query, String status) {
        Owner actor = getCurrentOwner();
        if (!isAdmin(actor) && !hasRole(actor, "ROLE_DOCTOR")) {
            throw new AccessDeniedException("Only doctor or admin can search medical chat pets.");
        }

        return petRepository.searchPetsWithMedicalChat(query == null ? null : query.trim())
                .stream()
                .filter(pet -> matchesStatusFilter(pet, status))
                .map(this::mapMedicalChatPetSearch)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedicalChatThreadResponseDto closeMedicalChat(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found for id: " + petId));
        Owner actor = getCurrentOwner();

        if (!canCloseChat(pet, actor)) {
            throw new AccessDeniedException("Only the pet owner or admin can close this medical chat.");
        }

        if (isChatClosed(pet)) {
            throw new IllegalArgumentException("This medical chat is already closed.");
        }

        pet.setMedicalChatStatus(CHAT_STATUS_CLOSED);
        pet.setMedicalChatClosedAt(java.time.LocalDateTime.now());
        pet.setMedicalChatClosedByUserId(actor.getId());
        pet.setMedicalChatClosedByName(actor.getOwnerName());
        petRepository.save(pet);

        PetMedicalChatMessage systemMessage = PetMedicalChatMessage.builder()
                .pet(pet)
                .sender(actor)
                .senderRole(resolvePrimaryRole(actor))
                .message("Medical chat closed by " + actor.getOwnerName() + ". Issue resolved.")
                .emergency(false)
                .build();
        petMedicalChatMessageRepository.save(systemMessage);

        return getMedicalChatThread(petId);
    }

    @Override
    @Transactional
    public MedicalChatMessageResponseDto sendMedicalChatMessage(Long petId, MedicalChatMessageRequestDto requestDto) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found for id: " + petId));
        Owner actor = getCurrentOwner();
        enforceConversationAccess(pet, actor);
        ensureChatOpenForReply(pet, actor);

        String safeMessage = requestDto.getMessage() == null ? "" : requestDto.getMessage().trim();
        if (safeMessage.isBlank() && requestDto.getLinkedImageId() == null) {
            throw new IllegalArgumentException("Message text or linked image is required.");
        }

        if (requestDto.getLinkedImageId() != null) {
            PetMedicalChatImage image = petMedicalChatImageRepository.findById(requestDto.getLinkedImageId())
                    .orElseThrow(() -> new RuntimeException("Linked image not found."));
            if (!image.getPet().getId().equals(petId)) {
                throw new IllegalArgumentException("Linked image does not belong to selected pet.");
            }
        }

        PetMedicalChatMessage entity = PetMedicalChatMessage.builder()
                .pet(pet)
                .sender(actor)
                .senderRole(resolvePrimaryRole(actor))
                .message(safeMessage.isBlank() ? null : safeMessage)
                .emergency(requestDto.isEmergency())
                .linkedImageId(requestDto.getLinkedImageId())
                .build();

        PetMedicalChatMessage saved = petMedicalChatMessageRepository.save(entity);
        if (saved.isEmergency()) {
            sendEmergencyTelegramNotification(pet, saved, 0);
        }
        return mapMessage(saved);
    }

    @Override
    @Transactional
    public MedicalChatMessageResponseDto uploadMedicalChatImages(Long petId,
                                                                 MultipartFile[] files,
                                                                 String message,
                                                                 boolean emergency) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found for id: " + petId));
        Owner actor = getCurrentOwner();

        if (!isAdmin(actor) && !pet.getOwner().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Only pet owner or admin can upload medical condition images.");
        }
        ensureChatOpenForReply(pet, actor);

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("At least one image is required.");
        }

        if (files.length > 10) {
            throw new IllegalArgumentException("You can upload up to 10 images per request.");
        }

        validateImageFiles(files);

        PetMedicalChatMessage chatMessage = PetMedicalChatMessage.builder()
                .pet(pet)
                .sender(actor)
                .senderRole(resolvePrimaryRole(actor))
                .message(message == null || message.trim().isBlank() ? null : message.trim())
                .emergency(emergency)
                .build();

        PetMedicalChatMessage savedMessage = petMedicalChatMessageRepository.save(chatMessage);

        List<PetMedicalChatImage> images = Arrays.stream(files)
                .map(file -> toImageEntity(file, pet, actor, savedMessage))
                .collect(Collectors.toList());

        savedMessage.setImages(images);
        PetMedicalChatMessage updated = petMedicalChatMessageRepository.save(savedMessage);

        if (emergency) {
            sendEmergencyTelegramNotification(pet, updated, images.size());
        }

        return mapMessage(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getMedicalChatImage(Long imageId) {
        PetMedicalChatImage image = petMedicalChatImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found for id: " + imageId));
        enforceConversationAccess(image.getPet(), getCurrentOwner());
        return image.getImageData();
    }

    @Override
    @Transactional(readOnly = true)
    public String getMedicalChatImageContentType(Long imageId) {
        PetMedicalChatImage image = petMedicalChatImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found for id: " + imageId));
        enforceConversationAccess(image.getPet(), getCurrentOwner());
        return image.getContentType();
    }

    @Override
    @Transactional
    public void assignVetToPet(Long petId, Long vetId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found for id: " + petId));
        Owner vet = ownerRepository.findById(vetId)
                .orElseThrow(() -> new RuntimeException("Vet user not found for id: " + vetId));

        boolean doctorRolePresent = vet.getRoles().stream()
                .anyMatch(role -> "ROLE_DOCTOR".equals(role.getName()));
        if (!doctorRolePresent) {
            throw new IllegalArgumentException("Selected user is not a doctor.");
        }

        pet.setAssignedVet(vet);
        petRepository.save(pet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalChatMessageResponseDto> getEmergencyMedicalChatFeed() {
        Owner actor = getCurrentOwner();
        boolean admin = isAdmin(actor);
        boolean doctor = hasRole(actor, "ROLE_DOCTOR");
        if (!admin && !doctor) {
            throw new AccessDeniedException("Only doctor or admin can access emergency feed.");
        }

        return petMedicalChatMessageRepository.findTop20ByEmergencyTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::mapMessage)
                .collect(Collectors.toList());
    }

    // ---- Thread-based operations ----

    @Override
    @Transactional(readOnly = true)
    public List<MedicalChatThreadSummaryDto> getThreadsForPet(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found for id: " + petId));
        Owner actor = getCurrentOwner();
        enforceConversationAccess(pet, actor);

        return petMedicalChatThreadRepository.findByPetIdOrderByCreatedAtDesc(petId)
                .stream()
                .map(this::mapThreadSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedicalChatThreadResponseDto createThread(Long petId, String title) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found for id: " + petId));
        Owner actor = getCurrentOwner();
        enforceConversationAccess(pet, actor);

        if (hasRole(actor, "ROLE_DOCTOR") && !isAdmin(actor)) {
            throw new AccessDeniedException("Doctors can reply in threads but cannot create new threads.");
        }

        String resolvedTitle = (title == null || title.isBlank())
                ? "Medical Case - " + pet.getPetName()
                : title.trim();

        PetMedicalChatThread thread = PetMedicalChatThread.builder()
                .pet(pet)
                .title(resolvedTitle)
                .status(CHAT_STATUS_ACTIVE)
                .createdBy(actor)
                .build();
        petMedicalChatThreadRepository.save(thread);
        return getThreadById(thread.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalChatThreadResponseDto getThreadById(Long threadId) {
        PetMedicalChatThread thread = petMedicalChatThreadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found for id: " + threadId));
        Owner actor = getCurrentOwner();
        enforceConversationAccess(thread.getPet(), actor);

        List<MedicalChatMessageResponseDto> messages = petMedicalChatMessageRepository
                .findByThreadIdOrderByCreatedAtAsc(threadId)
                .stream()
                .map(this::mapMessage)
                .collect(Collectors.toList());

        Pet pet = thread.getPet();
        Long petId = pet.getId();

        return MedicalChatThreadResponseDto.builder()
                .threadId(thread.getId())
                .petId(petId)
                .petName(pet.getPetName())
                .ownerId(pet.getOwner().getId())
                .ownerName(pet.getOwner().getOwnerName())
                .assignedVetId(pet.getAssignedVet() != null ? pet.getAssignedVet().getId() : null)
                .assignedVetName(pet.getAssignedVet() != null ? pet.getAssignedVet().getOwnerName() : null)
                .chatStatus(thread.getStatus())
                .closedAt(thread.getClosedAt())
                .closedByName(thread.getClosedByName())
                .canClose(canCloseThread(thread, actor))
                .canReply(!isThreadClosed(thread) || isAdmin(actor))
                .petContext(mapPetContext(pet))
                .vaccinationRecords(getVaccinationRecordsForPet(petId, false))
                .dewormingRecords(getVaccinationRecordsForPet(petId, true))
                .medicalRecords(getMedicalRecordsForPet(petId))
                .messages(messages)
                .build();
    }

    @Override
    @Transactional
    public MedicalChatThreadResponseDto closeThread(Long threadId) {
        PetMedicalChatThread thread = petMedicalChatThreadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found for id: " + threadId));
        Owner actor = getCurrentOwner();

        if (!canCloseThread(thread, actor)) {
            throw new AccessDeniedException("Only the pet owner or admin can close this medical thread.");
        }
        if (isThreadClosed(thread)) {
            throw new IllegalArgumentException("This medical thread is already closed.");
        }

        thread.setStatus(CHAT_STATUS_CLOSED);
        thread.setClosedAt(java.time.LocalDateTime.now());
        thread.setClosedByUserId(actor.getId());
        thread.setClosedByName(actor.getOwnerName());
        petMedicalChatThreadRepository.save(thread);

        PetMedicalChatMessage systemMessage = PetMedicalChatMessage.builder()
                .pet(thread.getPet())
                .thread(thread)
                .sender(actor)
                .senderRole(resolvePrimaryRole(actor))
                .message("Medical thread closed by " + actor.getOwnerName() + ". Issue resolved.")
                .emergency(false)
                .build();
        petMedicalChatMessageRepository.save(systemMessage);

        return getThreadById(threadId);
    }

    @Override
    @Transactional
    public MedicalChatMessageResponseDto sendMessageToThread(Long threadId, MedicalChatMessageRequestDto requestDto) {
        PetMedicalChatThread thread = petMedicalChatThreadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found for id: " + threadId));
        Owner actor = getCurrentOwner();
        enforceConversationAccess(thread.getPet(), actor);

        if (isThreadClosed(thread) && !isAdmin(actor)) {
            throw new IllegalArgumentException("This thread is closed. You can view it in history but cannot add new messages.");
        }

        String safeMessage = requestDto.getMessage() == null ? "" : requestDto.getMessage().trim();
        if (safeMessage.isBlank() && requestDto.getLinkedImageId() == null) {
            throw new IllegalArgumentException("Message text or linked image is required.");
        }

        PetMedicalChatMessage entity = PetMedicalChatMessage.builder()
                .pet(thread.getPet())
                .thread(thread)
                .sender(actor)
                .senderRole(resolvePrimaryRole(actor))
                .message(safeMessage.isBlank() ? null : safeMessage)
                .emergency(requestDto.isEmergency())
                .linkedImageId(requestDto.getLinkedImageId())
                .build();

        PetMedicalChatMessage saved = petMedicalChatMessageRepository.save(entity);
        if (saved.isEmergency()) {
            sendEmergencyTelegramNotification(thread.getPet(), saved, 0);
        }
        return mapMessage(saved);
    }

    @Override
    @Transactional
    public MedicalChatMessageResponseDto uploadImagesToThread(Long threadId,
                                                               MultipartFile[] files,
                                                               String message,
                                                               boolean emergency) {
        PetMedicalChatThread thread = petMedicalChatThreadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found for id: " + threadId));
        Owner actor = getCurrentOwner();

        if (!isAdmin(actor) && !thread.getPet().getOwner().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Only pet owner or admin can upload medical condition images.");
        }
        if (isThreadClosed(thread) && !isAdmin(actor)) {
            throw new IllegalArgumentException("This thread is closed. Image uploads are disabled.");
        }

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("At least one image is required.");
        }
        if (files.length > 10) {
            throw new IllegalArgumentException("You can upload up to 10 images per request.");
        }
        validateImageFiles(files);

        PetMedicalChatMessage chatMessage = PetMedicalChatMessage.builder()
                .pet(thread.getPet())
                .thread(thread)
                .sender(actor)
                .senderRole(resolvePrimaryRole(actor))
                .message(message == null || message.trim().isBlank() ? null : message.trim())
                .emergency(emergency)
                .build();

        PetMedicalChatMessage savedMessage = petMedicalChatMessageRepository.save(chatMessage);

        List<PetMedicalChatImage> images = Arrays.stream(files)
                .map(file -> toImageEntity(file, thread.getPet(), actor, savedMessage))
                .collect(Collectors.toList());

        savedMessage.setImages(images);
        PetMedicalChatMessage updated = petMedicalChatMessageRepository.save(savedMessage);

        if (emergency) {
            sendEmergencyTelegramNotification(thread.getPet(), updated, images.size());
        }
        return mapMessage(updated);
    }

    private void enforceConversationAccess(Pet pet, Owner actor) {
        boolean owner = pet.getOwner() != null && pet.getOwner().getId().equals(actor.getId());
        boolean doctor = hasRole(actor, "ROLE_DOCTOR");
        if (!owner && !doctor && !isAdmin(actor)) {
            throw new AccessDeniedException("You are not allowed to access this pet conversation.");
        }
    }

    private void ensureChatOpenForReply(Pet pet, Owner actor) {
        if (isChatClosed(pet) && !isAdmin(actor)) {
            throw new IllegalArgumentException("This medical chat is closed. You can view it in history but cannot add new messages.");
        }
    }

    private Owner getCurrentOwner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return ownerRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Logged-in user was not found"));
    }

    private boolean isAdmin(Owner owner) {
        return hasRole(owner, "ROLE_ADMIN");
    }

    private boolean isChatClosed(Pet pet) {
        return CHAT_STATUS_CLOSED.equalsIgnoreCase(resolveChatStatus(pet));
    }

    private String resolveChatStatus(Pet pet) {
        return (pet.getMedicalChatStatus() == null || pet.getMedicalChatStatus().isBlank())
                ? CHAT_STATUS_ACTIVE
                : pet.getMedicalChatStatus();
    }

    private boolean canCloseChat(Pet pet, Owner actor) {
        return isAdmin(actor) || (pet.getOwner() != null && pet.getOwner().getId().equals(actor.getId()));
    }

    private boolean canCloseThread(PetMedicalChatThread thread, Owner actor) {
        return isAdmin(actor) || (thread.getPet().getOwner() != null && thread.getPet().getOwner().getId().equals(actor.getId()));
    }

    private boolean isThreadClosed(PetMedicalChatThread thread) {
        return CHAT_STATUS_CLOSED.equalsIgnoreCase(thread.getStatus());
    }

    private MedicalChatThreadSummaryDto mapThreadSummary(PetMedicalChatThread thread) {
        java.util.Optional<PetMedicalChatMessage> latestMessage = petMedicalChatMessageRepository
                .findTopByThreadIdOrderByCreatedAtDesc(thread.getId());
        long count = petMedicalChatMessageRepository.countByThreadId(thread.getId());
        boolean hasEmergency = petMedicalChatMessageRepository.existsByThreadIdAndEmergencyTrue(thread.getId());

        return MedicalChatThreadSummaryDto.builder()
                .threadId(thread.getId())
                .petId(thread.getPet().getId())
                .petName(thread.getPet().getPetName())
                .title(thread.getTitle())
                .status(thread.getStatus())
                .createdAt(thread.getCreatedAt())
                .closedAt(thread.getClosedAt())
                .closedByName(thread.getClosedByName())
                .latestMessage(latestMessage.map(PetMedicalChatMessage::getMessage).orElse(null))
                .latestMessageAt(latestMessage.map(PetMedicalChatMessage::getCreatedAt).orElse(null))
                .hasEmergency(hasEmergency)
                .messageCount(count)
                .build();
    }

    private boolean canReplyToChat(Pet pet, Owner actor) {
        return !isChatClosed(pet) || isAdmin(actor);
    }

    private boolean matchesStatusFilter(Pet pet, String status) {
        String normalized = status == null ? CHAT_STATUS_ACTIVE : status.trim().toUpperCase();
        String chatStatus = resolveChatStatus(pet).toUpperCase();

        if ("ALL".equals(normalized)) {
            return true;
        }
        if ("HISTORY".equals(normalized) || CHAT_STATUS_CLOSED.equals(normalized)) {
            return CHAT_STATUS_CLOSED.equals(chatStatus);
        }
        return CHAT_STATUS_ACTIVE.equals(chatStatus);
    }

    private boolean hasRole(Owner owner, String roleName) {
        return owner.getRoles() != null && owner.getRoles().stream().anyMatch(role -> roleName.equals(role.getName()));
    }

    private String resolvePrimaryRole(Owner owner) {
        return owner.getRoles().stream().findFirst().map(Role::getName).orElse("ROLE_PET_OWNER");
    }

    private void validateImageFiles(MultipartFile[] files) {
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Empty files are not allowed.");
            }
            if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
                throw new IllegalArgumentException("Each image must be <= 5MB.");
            }
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                throw new IllegalArgumentException("Only JPG and PNG images are allowed.");
            }
        }
    }

    private PetMedicalChatImage toImageEntity(MultipartFile file, Pet pet, Owner actor, PetMedicalChatMessage chatMessage) {
        try {
            return PetMedicalChatImage.builder()
                    .chatMessage(chatMessage)
                    .pet(pet)
                    .sender(actor)
                    .fileName(file.getOriginalFilename() == null ? "medical-image" : file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .imageData(file.getBytes())
                    .build();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read uploaded image", ex);
        }
    }

    private MedicalChatMessageResponseDto mapMessage(PetMedicalChatMessage message) {
        List<MedicalChatImageResponseDto> images = message.getImages().stream()
                .sorted(Comparator.comparing(PetMedicalChatImage::getUploadedAt))
                .map(this::mapImage)
                .collect(Collectors.toList());

        return MedicalChatMessageResponseDto.builder()
                .id(message.getId())
                .petId(message.getPet().getId())
                .senderName(message.getSender().getOwnerName())
                .senderRole(message.getSenderRole())
                .message(message.getMessage())
                .emergency(message.isEmergency())
                .linkedImageId(message.getLinkedImageId())
                .createdAt(message.getCreatedAt())
                .images(images)
                .build();
    }

    private MedicalChatImageResponseDto mapImage(PetMedicalChatImage image) {
        return MedicalChatImageResponseDto.builder()
                .id(image.getId())
                .petId(image.getPet().getId())
                .fileName(image.getFileName())
                .contentType(image.getContentType())
                .sizeBytes(image.getSizeBytes())
                .senderName(image.getSender().getOwnerName())
                .senderRole(resolvePrimaryRole(image.getSender()))
                .uploadedAt(image.getUploadedAt())
                .imageUrl("/api/pets/medical-chat/images/" + image.getId())
                .build();
    }

    private MedicalChatPetContextDto mapPetContext(Pet pet) {
        return MedicalChatPetContextDto.builder()
                .id(pet.getId())
                .petName(pet.getPetName())
                .petType(pet.getPetType())
                .breed(pet.getBreed())
                .gender(pet.getGender())
                .dob(pet.getDob())
                .registrationDate(pet.getRegistrationDate())
                .description(pet.getDescription())
                .allergies(pet.getAllergies())
                .photoBase64(pet.getPhoto() != null && pet.getPhoto().length > 0
                        ? Base64.getEncoder().encodeToString(pet.getPhoto())
                        : null)
                .photoContentType(pet.getPhotoContentType())
                .ownerName(pet.getOwner() != null ? pet.getOwner().getOwnerName() : null)
                .ownerEmail(pet.getOwner() != null ? pet.getOwner().getEmail() : null)
                .ownerPhoneNumber(pet.getOwner() != null ? pet.getOwner().getPhoneNumber() : null)
                .assignedVetName(pet.getAssignedVet() != null ? pet.getAssignedVet().getOwnerName() : null)
                .build();
    }

    private List<PetVaccinationRecorResponsedDTO> getVaccinationRecordsForPet(Long petId, boolean dewormingOnly) {
        return vaccinationRecordRepository.findByPetIdOrderByVaccinationDateDesc(petId)
                .stream()
                .filter(record -> dewormingOnly == isDewormingRecord(record))
                .map(record -> {
                    PetVaccinationRecorResponsedDTO dto = modelMapper.map(record, PetVaccinationRecorResponsedDTO.class);
                    dto.setPetId(record.getPet().getId());
                    dto.setOwnerContact(record.getPet().getOwner().getPhoneNumber());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<PetMedicalRespnseDto> getMedicalRecordsForPet(Long petId) {
        return petMedicalRepository.findByPetIdOrderByVisitDateDesc(petId)
                .stream()
                .map(record -> {
                    PetMedicalRespnseDto dto = modelMapper.map(record, PetMedicalRespnseDto.class);
                    dto.setPetMedicalId(record.getPetMedicalId());
                    dto.setPetId(record.getPet().getId());
                    dto.setOwnerContact(record.getPet().getOwner().getPhoneNumber());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private boolean isDewormingRecord(PetVaccinationRecord record) {
        String name = ((record.getVaccination() == null ? "" : record.getVaccination()) + " "
                + (record.getVaccineName() == null ? "" : record.getVaccineName())).toLowerCase();
        return name.contains("deworm");
    }

    private MedicalChatPetSearchResponseDto mapMedicalChatPetSearch(Pet pet) {
        Optional<PetMedicalChatMessage> latestMessageOptional = petMedicalChatMessageRepository.findTopByPetIdOrderByCreatedAtDesc(pet.getId());
        Optional<PetMedicalChatThread> latestThreadOptional = petMedicalChatThreadRepository
                .findByPetIdOrderByCreatedAtDesc(pet.getId())
                .stream()
                .findFirst();

        return MedicalChatPetSearchResponseDto.builder()
                .petId(pet.getId())
                .petName(pet.getPetName())
                .petType(pet.getPetType())
                .breed(pet.getBreed())
                .ownerName(pet.getOwner() != null ? pet.getOwner().getOwnerName() : null)
                .ownerPhoneNumber(pet.getOwner() != null ? pet.getOwner().getPhoneNumber() : null)
                .assignedVetName(pet.getAssignedVet() != null ? pet.getAssignedVet().getOwnerName() : null)
                .latestMessageAt(latestMessageOptional.map(PetMedicalChatMessage::getCreatedAt).orElse(null))
                .latestMessage(latestMessageOptional.map(PetMedicalChatMessage::getMessage).orElse(null))
                .latestThreadId(latestThreadOptional.map(PetMedicalChatThread::getId).orElse(null))
                .latestThreadTitle(latestThreadOptional.map(PetMedicalChatThread::getTitle).orElse(null))
                .emergency(petMedicalChatMessageRepository.existsByPetIdAndEmergencyTrue(pet.getId()))
                .chatStatus(resolveChatStatus(pet))
                .closedAt(pet.getMedicalChatClosedAt())
                .closedByName(pet.getMedicalChatClosedByName())
                .build();
    }

    private void sendEmergencyTelegramNotification(Pet pet, PetMedicalChatMessage message, int imageCount) {
        String notification = "EMERGENCY PET CASE\n"
                + "Pet: " + pet.getPetName() + " (ID: " + pet.getId() + ")\n"
                + "Owner: " + pet.getOwner().getOwnerName() + "\n"
                + "Sender: " + message.getSender().getOwnerName() + " (" + message.getSenderRole() + ")\n"
                + "Images: " + imageCount + "\n"
                + "Message: " + (message.getMessage() == null ? "(no text)" : message.getMessage()) + "\n"
                + "Chat: /pet-medical-chat/" + pet.getId();
        telegramNotificationService.sendMessage(notification);
    }
}
