

/**
 * Does the envelope have a JSON Payload?
 *
 * @envelope target envelope
 * @return True if the Content-Type is application/json or null
 */
function isJsonPayload(envelope){

    var contentType = envelope.getHeader("Content-Type");

    return (contentType == null || contentType == "application/json");
}

/**
 * Convert the JSON payload of the envelope to an object.
 *
 * @envelope target envelope
 * @return Object representing payload or null if it could not be converted
 */
function convertPayload(envelope){

    var payload = new java.lang.String(envelope.getPayload());

    return JSON.parse(payload);
}