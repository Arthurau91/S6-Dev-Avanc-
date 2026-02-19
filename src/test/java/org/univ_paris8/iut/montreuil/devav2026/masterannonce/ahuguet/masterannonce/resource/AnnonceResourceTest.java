package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.*;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.NotFoundException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.UserSecurityContext;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.UserPrincipal;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service.AnnonceService;

import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnnonceResource.
 * Mocks the AnnonceService to test controller logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class AnnonceResourceTest {

    @Mock
    private AnnonceService annonceService;

    private AnnonceResource resource;
    private UserSecurityContext securityContext;

    @BeforeEach
    void setUp() {
        resource = new AnnonceResource(annonceService);
        securityContext = new UserSecurityContext(new UserPrincipal(1L, "testuser"), "USER", false);
    }

    @Test
    @DisplayName("GET /annonces - should return 200 with paginated data")
    void testListAnnonces() {
        AnnonceDTO dto = AnnonceDTO.builder()
                .id(1L).title("Test").description("Desc")
                .status("DRAFT").date(Timestamp.from(Instant.now())).build();
        PaginatedResponseDTO<AnnonceDTO> paginatedResponse =
                new PaginatedResponseDTO<>(Arrays.asList(dto), 1, 10, 1);

        when(annonceService.getAnnonces(any(), any(), any(), eq(1), eq(10))).thenReturn(paginatedResponse);

        Response response = resource.listAnnonces(null, null, null, 1, 10);

        assertEquals(200, response.getStatus());
        PaginatedResponseDTO<?> body = (PaginatedResponseDTO<?>) response.getEntity();
        assertEquals(1, body.getData().size());
    }

    @Test
    @DisplayName("GET /annonces/{id} - should return 200 when found")
    void testGetAnnonce_found() {
        AnnonceDTO dto = AnnonceDTO.builder().id(1L).title("Test").build();
        when(annonceService.getAnnonceById(1L)).thenReturn(dto);

        Response response = resource.getAnnonce(1L);

        assertEquals(200, response.getStatus());
        AnnonceDTO body = (AnnonceDTO) response.getEntity();
        assertEquals("Test", body.getTitle());
    }

    @Test
    @DisplayName("GET /annonces/{id} - should propagate NotFoundException")
    void testGetAnnonce_notFound() {
        when(annonceService.getAnnonceById(999L)).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> resource.getAnnonce(999L));
    }

    @Test
    @DisplayName("POST /annonces - should return 201 with created annonce")
    void testCreateAnnonce() {
        AnnonceDTO created = AnnonceDTO.builder().id(1L).title("New").build();
        when(annonceService.createAnnonce(any(AnnonceCreateDTO.class), eq(1L))).thenReturn(created);

        AnnonceCreateDTO dto = new AnnonceCreateDTO();
        dto.setTitle("New");
        dto.setDescription("Description");

        Response response = resource.createAnnonce(dto, securityContext);

        assertEquals(201, response.getStatus());
        AnnonceDTO body = (AnnonceDTO) response.getEntity();
        assertEquals("New", body.getTitle());
    }

    @Test
    @DisplayName("PUT /annonces/{id} - should return 200 with updated annonce")
    void testUpdateAnnonce() {
        AnnonceDTO updated = AnnonceDTO.builder().id(1L).title("Updated").build();
        when(annonceService.updateAnnonce(eq(1L), any(AnnonceUpdateDTO.class), eq(1L))).thenReturn(updated);

        AnnonceUpdateDTO dto = new AnnonceUpdateDTO();
        dto.setTitle("Updated");
        dto.setDescription("Updated Desc");
        dto.setVersion(0L);

        Response response = resource.updateAnnonce(1L, dto, securityContext);

        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("DELETE /annonces/{id} - should return 204")
    void testDeleteAnnonce() {
        doNothing().when(annonceService).deleteAnnonce(1L, 1L);

        Response response = resource.deleteAnnonce(1L, securityContext);

        assertEquals(204, response.getStatus());
        verify(annonceService).deleteAnnonce(1L, 1L);
    }
}
