"oauth2ProviderData" : {
  "id": "oauth2ProviderData",
  "uniqueItems": false,
  "properties": {
    "serviceName": {
      "uniqueItems": false,
      "type": "string",
      "required": true
    },
    "enabled": {
      "uniqueItems": false,
      "type": "boolean",
      "required": true
    },
    "clientId": {
      "uniqueItems": false,
      "type": "string",
      "required": true
    },
    "scopes": {
      "uniqueItems": false,
      "type": "array",
      "required": true
    },
    "authorizationURL": {
      "uniqueItems": false,
      "type": "string",
      "required": true
    },
    "authorizationServerURL": {
      "uniqueItems": false,
      "type": "string",
      "required": true
    },
    "tokenServerURL": {
      "uniqueItems": false,
      "type": "string",
      "required": true
    },
    "hasToken": {
      "uniqueItems": false,
      "type": "boolean",
      "required": true
    },
    "userId": {
      "uniqueItems": false,
      "type": "string",
      "required": true
    }
  }
}
