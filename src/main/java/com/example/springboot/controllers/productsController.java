package com.example.springboot.controllers;

import com.example.springboot.dtos.productRecordDto;
import com.example.springboot.models.productsModel;
import com.example.springboot.repositories.productRepository;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

@RestController
public class productsController {

    @Autowired
    productRepository productRepository;

    @PostMapping("/products")
    public ResponseEntity<productsModel> saveProduct(@RequestBody @Valid productRecordDto productRecordDto) {
        var productModel = new productsModel();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }

    @GetMapping("/products")
    public ResponseEntity<List<productsModel>> getAllProducts(){
        List<productsModel> productsList = productRepository.findAll();
        if(!productsList.isEmpty()) {
            for(productsModel product : productsList) {
                UUID id = product.getIdProduct();
                product.add(linkTo(methodOn(productsController.class).getOneProduct(id)).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(productsList);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getOneProduct(@PathVariable(value="id") UUID id){
        Optional<productsModel> productO = productRepository.findById(id);
        if(productO.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        productO.get().add(linkTo(methodOn(productsController.class).getAllProducts()).withRel("Products List"));
        return ResponseEntity.status(HttpStatus.OK).body(productO.get());
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value="id") UUID id,
                                                @RequestBody @Valid productRecordDto ProductRecordDto) {
        Optional<productsModel> productO = productRepository.findById(id);
        if(productO.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        var productsModel = productO.get();
        BeanUtils.copyProperties(ProductRecordDto, productsModel);
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productsModel));
    }
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value="id") UUID id) {
        Optional<productsModel> productO = productRepository.findById(id);
        if(productO.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        productRepository.delete(productO.get());
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully.");
    }
}
