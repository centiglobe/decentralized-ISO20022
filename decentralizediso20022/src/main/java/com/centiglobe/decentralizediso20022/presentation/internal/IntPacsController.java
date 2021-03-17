package com.centiglobe.decentralizediso20022.presentation.internal;

import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

import com.centiglobe.decentralizediso20022.annotation.ApiVersion;

import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller for handling internal pacs messages
 * 
 * @author William Stackenäs
 */
@RestController
@Profile("internal")
@ApiVersion(1)
@RequestMapping("pacs")
public class IntPacsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntPacsController.class);

    @GetMapping(value = {"/008", "/008/{var}", "/008/{var}/{ver}"})
    public Map<String, String> getPacs008(@PathVariable(required = false) String var,
                             @PathVariable(required = false) String ver) {
        String resp = "Get request for internal /pacs/008/" + var + "/" + ver;
        LOGGER.debug(resp);
        return Collections.singletonMap("response", resp);
    }

    @PostMapping("/007/{var}/{ver}")
    public String handlePacs008() {
        return "OK";
    }
}
