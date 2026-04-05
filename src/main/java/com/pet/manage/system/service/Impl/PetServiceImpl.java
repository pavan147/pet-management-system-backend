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

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PetServiceImpl implements PetService {

    // ── PDF colour palette ────────────────────────────────────────────────
    private static final Color C_PRIMARY     = new Color(10,  102,  94);
    private static final Color C_PRIMARY_MED = new Color(13,  148, 136);
    private static final Color C_PRIMARY_LT  = new Color(204, 251, 241);
    private static final Color C_ORANGE      = new Color(194,  65,  12);
    private static final Color C_ORANGE_LT   = new Color(255, 237, 213);
    private static final Color C_GRAY_BG     = new Color(248, 250, 252);
    private static final Color C_BORDER      = new Color(203, 213, 225);
    private static final Color C_TEXT        = new Color( 15,  23,  42);
    private static final Color C_MUTED       = new Color(100, 116, 139);
    private static final Color C_LABEL       = new Color( 71,  85, 105);
    // ─────────────────────────────────────────────────────────────────────

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
        Pet   pet = record.getPet();
        Owner own = (pet != null) ? pet.getOwner() : null;
        try {
            Document doc = new Document(PageSize.A4, 45f, 45f, 40f, 50f);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, out);
            doc.open();

            pdfHeader(doc);
            pdfRecordBadge(doc, record);
            pdfDetailsSection(doc, pet, own);
            pdfDiagnosisSection(doc, record, pet);
            if (record.getPrescriptions() != null && !record.getPrescriptions().isEmpty()) {
                pdfMedicationsTable(doc, record.getPrescriptions());
            }
            pdfDocumentFooter(doc, record);

            doc.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate prescription PDF", ex);
        }
    }

    // ── Font helper ──────────────────────────────────────────────────────
    private Font f(float size, int style, Color color) {
        return FontFactory.getFont(FontFactory.HELVETICA, size, style, color);
    }

    // ── HEADER ───────────────────────────────────────────────────────────
    private void pdfHeader(Document doc) throws DocumentException {
        // Dark teal main header
        PdfPTable hdr = new PdfPTable(1);
        hdr.setWidthPercentage(100);
        PdfPCell hc = new PdfPCell();
        hc.setBackgroundColor(C_PRIMARY);
        hc.setBorder(Rectangle.NO_BORDER);
        hc.setPaddingTop(22f);
        hc.setPaddingBottom(16f);

        Paragraph clinicName = new Paragraph("PawCare Veterinary Hospital",
                f(22f, Font.BOLD, Color.WHITE));
        clinicName.setAlignment(Element.ALIGN_CENTER);
        hc.addElement(clinicName);

        Paragraph tagline = new Paragraph("Your Trusted Pet Healthcare Partner",
                f(10f, Font.ITALIC, new Color(167, 243, 208)));
        tagline.setAlignment(Element.ALIGN_CENTER);
        tagline.setSpacingBefore(4f);
        hc.addElement(tagline);

        Paragraph contact = new Paragraph(
                "Tel: +91-9876543210   |   care@pawcare.vet   |   www.pawcare.vet",
                f(8f, Font.NORMAL, new Color(167, 243, 208)));
        contact.setAlignment(Element.ALIGN_CENTER);
        contact.setSpacingBefore(5f);
        hc.addElement(contact);

        hdr.addCell(hc);
        doc.add(hdr);

        // Mid teal "MEDICAL PRESCRIPTION" sub-bar
        PdfPTable sub = new PdfPTable(1);
        sub.setWidthPercentage(100);
        sub.setSpacingAfter(10f);
        PdfPCell sc = new PdfPCell();
        sc.setBackgroundColor(C_PRIMARY_MED);
        sc.setBorder(Rectangle.NO_BORDER);
        sc.setPaddingTop(7f);
        sc.setPaddingBottom(7f);
        Paragraph presTitle = new Paragraph("MEDICAL PRESCRIPTION",
                f(12f, Font.BOLD, Color.WHITE));
        presTitle.setAlignment(Element.ALIGN_CENTER);
        sc.addElement(presTitle);
        sub.addCell(sc);
        doc.add(sub);
    }

    // ── RECORD BADGE (ID + dates) ─────────────────────────────────────────
    private void pdfRecordBadge(Document doc, PetMedical record) throws DocumentException {
        PdfPTable tbl = new PdfPTable(new float[]{1f, 1f});
        tbl.setWidthPercentage(100);
        tbl.setSpacingAfter(8f);

        PdfPCell left = new PdfPCell();
        left.setBackgroundColor(C_GRAY_BG);
        left.setBorderColor(C_BORDER);
        left.setBorderWidth(0.5f);
        left.setPadding(10f);
        Paragraph recId = new Paragraph();
        recId.add(new Chunk("RECORD #   ", f(8f, Font.NORMAL, C_MUTED)));
        recId.add(new Chunk(safe(record.getPetMedicalId()), f(13f, Font.BOLD, C_PRIMARY)));
        left.addElement(recId);
        tbl.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBackgroundColor(C_GRAY_BG);
        right.setBorderColor(C_BORDER);
        right.setBorderWidth(0.5f);
        right.setPadding(10f);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph dates = new Paragraph();
        dates.setAlignment(Element.ALIGN_RIGHT);
        dates.add(new Chunk("Visit Date: ", f(8f, Font.NORMAL, C_MUTED)));
        dates.add(new Chunk(safe(record.getVisitDate()) + "    ", f(9f, Font.BOLD, C_TEXT)));
        dates.add(new Chunk("Valid Till: ", f(8f, Font.NORMAL, C_MUTED)));
        dates.add(new Chunk(safe(record.getValidateTill()), f(9f, Font.BOLD, C_ORANGE)));
        right.addElement(dates);
        tbl.addCell(right);

        doc.add(tbl);
    }

    // ── PET + OWNER SECTION ───────────────────────────────────────────────
    private void pdfDetailsSection(Document doc, Pet pet, Owner own) throws DocumentException {
        PdfPTable wrapper = new PdfPTable(new float[]{1f, 1f});
        wrapper.setWidthPercentage(100);
        wrapper.setSpacingAfter(8f);

        // Pet details inner table
        PdfPTable petTbl = new PdfPTable(new float[]{1f, 1.8f});
        petTbl.setWidthPercentage(100);
        addSectionHdrRow(petTbl, "PET DETAILS", 2, C_PRIMARY);
        addInfoRow(petTbl, "Name",   pet != null ? safe(pet.getPetName()) : "N/A");
        addInfoRow(petTbl, "Type",   pet != null ? safe(pet.getPetType()) : "N/A");
        addInfoRow(petTbl, "Breed",  pet != null ? safe(pet.getBreed())   : "N/A");
        addInfoRow(petTbl, "Gender", pet != null ? safe(pet.getGender())  : "N/A");
        addInfoRow(petTbl, "DOB",    pet != null ? safe(pet.getDob())     : "N/A");
        PdfPCell petCell = new PdfPCell(petTbl);
        petCell.setBorder(Rectangle.NO_BORDER);
        petCell.setPaddingRight(4f);
        wrapper.addCell(petCell);

        // Owner details inner table
        PdfPTable ownTbl = new PdfPTable(new float[]{1f, 1.8f});
        ownTbl.setWidthPercentage(100);
        addSectionHdrRow(ownTbl, "OWNER DETAILS", 2, C_PRIMARY_MED);
        addInfoRow(ownTbl, "Name",    own != null ? safe(own.getOwnerName())   : "N/A");
        addInfoRow(ownTbl, "Contact", own != null ? safe(own.getPhoneNumber()) : "N/A");
        addInfoRow(ownTbl, "Email",   own != null ? safe(own.getEmail())       : "N/A");
        addInfoRow(ownTbl, "Address", own != null ? safe(own.getAddress())     : "N/A");
        PdfPCell ownCell = new PdfPCell(ownTbl);
        ownCell.setBorder(Rectangle.NO_BORDER);
        ownCell.setPaddingLeft(4f);
        wrapper.addCell(ownCell);

        doc.add(wrapper);
    }

    // ── DIAGNOSIS & TREATMENT ─────────────────────────────────────────────
    private void pdfDiagnosisSection(Document doc, PetMedical record, Pet pet) throws DocumentException {
        doc.add(sectionBar("DIAGNOSIS & TREATMENT", C_ORANGE));

        PdfPTable tbl = new PdfPTable(new float[]{1f, 3.5f});
        tbl.setWidthPercentage(100);
        tbl.setSpacingAfter(8f);

        addHighlightRow(tbl, "Diagnosis",
                safe(record.getDiagnosis()), C_ORANGE_LT, f(9f, Font.BOLD, C_ORANGE));

        String allergies = "None";
        if (record.getAllergies() != null && !record.getAllergies().isBlank()) {
            allergies = record.getAllergies();
        } else if (pet != null && pet.getAllergies() != null && !pet.getAllergies().isBlank()) {
            allergies = pet.getAllergies();
        }
        addHighlightRow(tbl, "Allergies",      allergies,                            C_GRAY_BG, f(9f, Font.NORMAL, C_TEXT));
        addHighlightRow(tbl, "Treatment Plan", safe(record.getTreatmentSuggestions()), C_GRAY_BG, f(9f, Font.NORMAL, C_TEXT));

        doc.add(tbl);
    }

    // ── PRESCRIBED MEDICATIONS TABLE ──────────────────────────────────────
    private void pdfMedicationsTable(Document doc, List<Prescription> prescriptions) throws DocumentException {
        doc.add(sectionBar("PRESCRIBED MEDICATIONS", C_PRIMARY));

        PdfPTable tbl = new PdfPTable(new float[]{0.4f, 2.2f, 1f, 1f, 1f, 1f, 1.6f, 2f});
        tbl.setWidthPercentage(100);
        tbl.setSpacingAfter(10f);

        // Header row
        for (String h : new String[]{"#", "Medicine", "Dosage", "Frequency", "Duration", "Meal", "Timing (M/A/E/N)", "Instructions"}) {
            PdfPCell hc = new PdfPCell(new Phrase(h, f(8f, Font.BOLD, Color.WHITE)));
            hc.setBackgroundColor(C_PRIMARY_MED);
            hc.setBorder(Rectangle.NO_BORDER);
            hc.setPadding(6f);
            hc.setHorizontalAlignment(Element.ALIGN_CENTER);
            tbl.addCell(hc);
        }

        // Data rows
        boolean alt = false;
        int idx = 1;
        for (Prescription p : prescriptions) {
            Color bg = alt ? C_PRIMARY_LT : Color.WHITE;
            String timing = safe(p.getMorning()) + "/" + safe(p.getAfternoon())
                          + "/" + safe(p.getEvening()) + "/" + safe(p.getNight());
            medCell(tbl, String.valueOf(idx++),                  bg, Element.ALIGN_CENTER);
            medCell(tbl, safe(p.getMedicine()),                  bg, Element.ALIGN_LEFT);
            medCell(tbl, safe(p.getDosage()),                    bg, Element.ALIGN_CENTER);
            medCell(tbl, safe(p.getFrequency()) + " x/day",     bg, Element.ALIGN_CENTER);
            medCell(tbl, safe(p.getDuration())  + " days",      bg, Element.ALIGN_CENTER);
            medCell(tbl, safe(p.getMeal()),                      bg, Element.ALIGN_CENTER);
            medCell(tbl, timing,                                  bg, Element.ALIGN_CENTER);
            medCell(tbl, safe(p.getInstructions()),              bg, Element.ALIGN_LEFT);
            alt = !alt;
        }
        doc.add(tbl);
    }

    // ── FOOTER ────────────────────────────────────────────────────────────
    private void pdfDocumentFooter(Document doc, PetMedical record) throws DocumentException {
        // Thin separator line via a 0-height table
        PdfPTable sep = new PdfPTable(1);
        sep.setWidthPercentage(100);
        sep.setSpacingBefore(10f);
        PdfPCell sc = new PdfPCell();
        sc.setBorderWidthTop(0.8f);
        sc.setBorderColorTop(C_BORDER);
        sc.setBorderWidthBottom(0f);
        sc.setBorderWidthLeft(0f);
        sc.setBorderWidthRight(0f);
        sc.setMinimumHeight(0f);
        sc.setPaddingTop(0f);
        sc.setPaddingBottom(0f);
        sep.addCell(sc);
        doc.add(sep);

        // Validity note
        Paragraph note = new Paragraph(
                "This prescription is valid till " + safe(record.getValidateTill())
                + ". Please consult your veterinarian before making any changes.",
                f(8f, Font.ITALIC, C_MUTED));
        note.setAlignment(Element.ALIGN_CENTER);
        note.setSpacingBefore(6f);
        doc.add(note);

        // Signature table
        PdfPTable sigTbl = new PdfPTable(new float[]{1f, 1f});
        sigTbl.setWidthPercentage(80f);
        sigTbl.setHorizontalAlignment(Element.ALIGN_CENTER);
        sigTbl.setSpacingBefore(24f);
        sigTbl.setSpacingAfter(6f);

        PdfPCell docCell = new PdfPCell();
        docCell.setBorder(Rectangle.TOP);
        docCell.setBorderColorTop(C_PRIMARY);
        docCell.setBorderWidthTop(1.5f);
        docCell.setPaddingTop(6f);
        docCell.setPaddingLeft(10f);
        docCell.addElement(new Phrase("Veterinarian's Signature", f(8f, Font.NORMAL, C_MUTED)));
        sigTbl.addCell(docCell);

        PdfPCell stampCell = new PdfPCell();
        stampCell.setBorder(Rectangle.TOP);
        stampCell.setBorderColorTop(C_PRIMARY);
        stampCell.setBorderWidthTop(1.5f);
        stampCell.setPaddingTop(6f);
        stampCell.setPaddingRight(10f);
        Paragraph sp = new Paragraph("Clinic Stamp", f(8f, Font.NORMAL, C_MUTED));
        sp.setAlignment(Element.ALIGN_RIGHT);
        stampCell.addElement(sp);
        sigTbl.addCell(stampCell);
        doc.add(sigTbl);

        // Generated-on line
        Paragraph gen = new Paragraph(
                "Generated on: " + LocalDate.now()
                + "   |   PawCare Veterinary Hospital Management System",
                f(7f, Font.NORMAL, C_MUTED));
        gen.setAlignment(Element.ALIGN_CENTER);
        doc.add(gen);
    }

    // ── Section bar (full-width coloured heading) ─────────────────────────
    private PdfPTable sectionBar(String title, Color bg) throws DocumentException {
        PdfPTable tbl = new PdfPTable(1);
        tbl.setWidthPercentage(100);
        tbl.setSpacingBefore(5f);
        tbl.setSpacingAfter(0f);
        PdfPCell cell = new PdfPCell(new Phrase(title, f(10f, Font.BOLD, Color.WHITE)));
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(7f);
        cell.setPaddingBottom(7f);
        cell.setPaddingLeft(10f);
        tbl.addCell(cell);
        return tbl;
    }

    // ── Section header row spanning inside a nested table ────────────────
    private void addSectionHdrRow(PdfPTable tbl, String title, int colspan, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(title, f(9f, Font.BOLD, Color.WHITE)));
        cell.setColspan(colspan);
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(6f);
        cell.setPaddingBottom(6f);
        cell.setPaddingLeft(8f);
        tbl.addCell(cell);
    }

    // ── Label / value row for info tables ─────────────────────────────────
    private void addInfoRow(PdfPTable tbl, String label, String value) {
        PdfPCell lc = new PdfPCell(new Phrase(label, f(8f, Font.BOLD, C_LABEL)));
        lc.setBackgroundColor(C_GRAY_BG);
        lc.setPadding(6f);
        lc.setBorderColor(C_BORDER);
        lc.setBorderWidth(0.5f);
        tbl.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value, f(9f, Font.NORMAL, C_TEXT)));
        vc.setBackgroundColor(Color.WHITE);
        vc.setPadding(6f);
        vc.setBorderColor(C_BORDER);
        vc.setBorderWidth(0.5f);
        tbl.addCell(vc);
    }

    // ── Highlighted label-value row (diagnosis section) ───────────────────
    private void addHighlightRow(PdfPTable tbl, String label, String value,
                                  Color valueBg, Font valueFont) {
        PdfPCell lc = new PdfPCell(new Phrase(label, f(8.5f, Font.BOLD, C_LABEL)));
        lc.setBackgroundColor(C_GRAY_BG);
        lc.setPadding(8f);
        lc.setBorderColor(C_BORDER);
        lc.setBorderWidth(0.5f);
        tbl.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value, valueFont));
        vc.setBackgroundColor(valueBg);
        vc.setPadding(8f);
        vc.setBorderColor(C_BORDER);
        vc.setBorderWidth(0.5f);
        tbl.addCell(vc);
    }

    // ── Medication table cell ─────────────────────────────────────────────
    private void medCell(PdfPTable tbl, String text, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, f(8f, Font.NORMAL, C_TEXT)));
        cell.setBackgroundColor(bg);
        cell.setPadding(5.5f);
        cell.setBorderColor(C_BORDER);
        cell.setBorderWidth(0.5f);
        cell.setHorizontalAlignment(align);
        tbl.addCell(cell);
    }

    private String safe(Object value) {
        return value == null ? "N/A" : value.toString();
    }
}
