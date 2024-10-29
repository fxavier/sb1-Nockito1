package com.ecommerce.resource;

import com.ecommerce.domain.dto.ProductImageUploadDTO;
import com.ecommerce.service.ImageStorageService;
import com.ecommerce.service.ProductService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.MultipartForm;

@Path("/api/products/{productId}/images")
public class ProductImageResource {
    
    @Inject
    ProductService productService;
    
    @Inject
    ImageStorageService imageStorageService;
    
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> uploadImage(
            @PathParam("productId") Long productId,
            @MultipartForm ProductImageUploadDTO uploadDTO) {
        return imageStorageService.uploadImage(
                uploadDTO.getFile().fileName(),
                uploadDTO.getFile().uploadedFile().toFile(),
                uploadDTO.getFile().contentType(),
                uploadDTO.getFile().size())
            .chain(imageUrl -> 
                productService.addProductImage(productId, imageUrl, uploadDTO.getIsCover()))
            .onItem().transform(image -> 
                Response.status(Response.Status.CREATED).entity(image).build());
    }
    
    @DELETE
    @Path("/{imageId}")
    public Uni<Response> deleteImage(
            @PathParam("productId") Long productId,
            @PathParam("imageId") Long imageId) {
        return productService.deleteProductImage(productId, imageId)
            .onItem().transform(deleted -> 
                deleted ? Response.noContent().build() 
                       : Response.status(Response.Status.NOT_FOUND).build());
    }
    
    @PUT
    @Path("/{imageId}/cover")
    public Uni<Response> setCoverImage(
            @PathParam("productId") Long productId,
            @PathParam("imageId") Long imageId) {
        return productService.setCoverImage(productId, imageId)
            .onItem().transform(image -> Response.ok(image).build());
    }
}