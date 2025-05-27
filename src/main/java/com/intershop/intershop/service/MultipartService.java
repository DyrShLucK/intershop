package com.intershop.intershop.service;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class MultipartService {

    public Optional<String> getFormField(MultiValueMap<String, Part> multipartData, String fieldName) {
        return Optional.ofNullable(multipartData.getFirst(fieldName))
                .filter(part -> part instanceof FormFieldPart)
                .map(part -> ((FormFieldPart) part).value());
    }

    public Mono<byte[]> extractImageBytes(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return Mono.just(bytes);
                });
    }
}