package com.pet.manage.system.controller;

import com.pet.manage.system.dtos.PetMedicalRequestDto;
import com.pet.manage.system.dtos.PetMedicalRespnseDto;
import com.pet.manage.system.service.PetService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetControllerTest {

    @Mock
    private PetService petService;

    @Mock
    private Validator validator;

    @InjectMocks
    private PetController petController;

    @Test
    void downloadPrescriptionPdf_shouldReturnAttachmentResponse() {
        Long petId = 11L;
        Long petMedicalId = 55L;
        byte[] expectedPdf = "pdf-content".getBytes();

        when(petService.downloadPrescriptionPdf(petId, petMedicalId)).thenReturn(expectedPdf);

        ResponseEntity<byte[]> response = petController.downloadPrescriptionPdf(petId, petMedicalId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(expectedPdf, response.getBody());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertEquals(
                "attachment; filename=\"pet_11_medical_55_prescription.pdf\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
        );
        verify(petService).downloadPrescriptionPdf(petId, petMedicalId);
    }

    @Test
    void savePetMedicalDetails_shouldReturnServiceResponse() {
        PetMedicalRequestDto request = new PetMedicalRequestDto();
        PetMedicalRespnseDto expected = new PetMedicalRespnseDto();
        expected.setPetId(9L);
        expected.setPetMedicalId(100L);

        when(petService.savePetMedicalDetails(request)).thenReturn(expected);

        ResponseEntity<PetMedicalRespnseDto> response = petController.savePetMedicalDetails(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(petService).savePetMedicalDetails(request);
    }
}

