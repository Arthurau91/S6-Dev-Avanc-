package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Generic paginated response wrapper.
 */
@Schema(description = "Paginated response")
public class PaginatedResponseDTO<T> {

    @Schema(description = "List of items in the current page")
    private List<T> data;

    @Schema(description = "Current page number (1-based)", example = "1")
    private int page;

    @Schema(description = "Page size", example = "10")
    private int pageSize;

    @Schema(description = "Total number of items", example = "42")
    private long totalItems;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    public PaginatedResponseDTO() {}

    public PaginatedResponseDTO(List<T> data, int page, int pageSize, long totalItems) {
        this.data = data;
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
