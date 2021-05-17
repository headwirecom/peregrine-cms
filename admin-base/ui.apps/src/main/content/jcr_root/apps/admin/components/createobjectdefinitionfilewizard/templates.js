export const schema = `{
    "type": "object",
    "properties": {
        "myRequiredProp": {
        "type": "string"
        },
        "myNonRequiredProp": {
        "type": "number"
        }
    },
    "required": ["myRequiredProp"]
}`;

export const uiSchema = `{
    "type": "VerticalLayout",
    "elements": [
      {
        "type": "Control",
        "scope": "#/properties/myRequiredProp"
      },
      {
        "type": "Control",
        "scope": "#/properties/myNonRequiredProp"
      }
    ]
}`;
