/*
 * My-Wine-Cellar, copyright 2020
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 */

package info.mywinecellar.api;

import info.mywinecellar.api.exception.ApiException;
import info.mywinecellar.converter.WineConverter;
import info.mywinecellar.dto.WineDto;
import info.mywinecellar.json.Builder;
import info.mywinecellar.json.MyWineCellar;
import info.mywinecellar.model.Producer;
import info.mywinecellar.model.Wine;
import info.mywinecellar.service.ClosureService;
import info.mywinecellar.service.ColorService;
import info.mywinecellar.service.ProducerService;
import info.mywinecellar.service.ShapeService;
import info.mywinecellar.service.TypeService;
import info.mywinecellar.service.WineService;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
@RestController
@RequestMapping("${apiPrefix}/wine")
public class WineRestController {

    private final ProducerService producerService;
    private final WineService wineService;
    private final ClosureService closureService;
    private final ColorService colorService;
    private final ShapeService shapeService;
    private final TypeService typeService;

    /**
     * Add a new wine
     *
     * @param request    Name, vintage, and size are required in the request:
     *                   {@link WineDto}
     *                   {@link WineConverter}
     * @param producerId The id of the producer for the new wine
     * @param shapeId    The id of the shape
     * @param colorId    The id of the color
     * @param typeId     The id of the type
     * @param closureId  The id of the closure
     * @return MyWineCellar JSON envelope and the wine
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/new")
    public MyWineCellar wineNewPost(@RequestBody(required = false) WineDto request, @RequestParam Long producerId,
                                    @RequestParam(defaultValue = "1") Long shapeId,
                                    @RequestParam(defaultValue = "1") Long colorId,
                                    @RequestParam(defaultValue = "1") Long typeId,
                                    @RequestParam(defaultValue = "1") Long closureId) {
        if (request != null) {
            Wine entity = WineConverter.toEntity(null, request);

            Producer producer = producerService.findById(producerId);
            entity.setProducer(producer);
            producer.getWines().add(entity);

            entity.setShape(shapeService.findById(shapeId));
            entity.setColor(colorService.findById(colorId));
            entity.setType(typeService.findById(typeId));
            entity.setClosure(closureService.findById(closureId));
            wineService.save(entity);

            return new Builder().wine(entity).build();
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "wine request was null");
        }
    }

    /**
     * Edit a wine
     *
     * @param wineId  The id of the wine to edit
     * @param request Variety of fields are available in the request:
     *                {@link WineDto}
     *                {@link WineConverter}
     * @return MyWineCellar JSON envelope and the wine
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/{wineId}/edit")
    public MyWineCellar wineEditPut(@PathVariable Long wineId, @RequestBody(required = false) WineDto request) {
        if (request != null) {
            Wine entity = WineConverter.toEntity(wineService.findById(wineId), request);
            wineService.update(entity);
            return new Builder().wine(entity).build();
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, String.format("wine request for id %d was null", wineId));
        }

    }

    /**
     * Add an image to the wine
     *
     * @param wineId The id of the wine
     * @param file   MultipartFile as a jpg, png, etc.
     * @return MyWineCellar JSON envelope and the wine
     * @throws IOException exception
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping(value = "/{wineId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MyWineCellar wineImagePut(@PathVariable Long wineId, @RequestPart(required = false) MultipartFile file) throws IOException {
        if (file != null) {
            Wine entity = wineService.findById(wineId);
            if (file.getBytes().length >= 5242880L) {
                log.debug("image file exceeded length of 5242880L, size equal to 5MB");
                throw new ApiException(HttpStatus.BAD_REQUEST, "image cannot exceed 5MB");
            }
            entity.setImage(file.getBytes());
            wineService.update(entity);
            return new Builder().wine(entity).build();
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "image file was included on the request");
        }
    }

}
