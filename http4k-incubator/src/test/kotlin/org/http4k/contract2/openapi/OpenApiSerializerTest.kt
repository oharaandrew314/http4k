package org.http4k.contract2.openapi

import org.http4k.core.ContentType
import org.http4k.core.Uri
import org.http4k.format.MoshiYaml
import org.http4k.testing.Approver
import org.http4k.testing.YamlApprovalTest
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(YamlApprovalTest::class)
class OpenApiSerializerTest {

    @Test
    fun `serialize PetStore`(approval: Approver) {
        val petStore = OpenApiDto(
            openapi = "3.0.3",
            info = OpenApiInfoDto(
                title = "Swagger Petstore - OpenAPI 3.0",
                version = "1.0.11",
                description = """This is a sample Pet Store Server based on the OpenAPI 3.0 specification.  You can find out more about
Swagger at [https://swagger.io](https://swagger.io). In the third iteration of the pet store, we've switched to the design first approach!
You can now help us improve the API whether it's by making changes to the definition itself or to the code.
That way, with time, we can improve the API in general, and expose some of the new features in OAS3.

_If you're looking for the Swagger 2.0/OAS 2.0 version of Petstore, then click [here](https://editor.swagger.io/?url=https://petstore.swagger.io/v2/swagger.yaml). Alternatively, you can load via the `Edit > Load Petstore OAS 2.0` menu option!_

Some useful links:
- [The Pet Store repository](https://github.com/swagger-api/swagger-petstore)
- [The source API definition for the Pet Store](https://github.com/swagger-api/swagger-petstore/blob/master/src/main/resources/openapi.yaml)""",
                termsOfService = Uri.of("http://swagger.io/terms/"),
                contact = OpenApiContactDto(
                    email = "apiteam@swagger.io",
                ),
                license = OpenApiLicenseDto(
                    name = "Apache 2.0",
                    url = Uri.of("http://www.apache.org/licenses/LICENSE-2.0.html"),
                )
            ),
            externalDocs = OpenApiExternalDocsDto(
                description = "Find out more about Swagger",
                url = Uri.of("http://swagger.io")
            ),
            servers = listOf(
                OpenApiServerDto(
                    url = Uri.of("https://petstore3.swagger.io/api/v3"),
                )
            ),
            tags = listOf(
                OpenApiTagDto(
                    name = "pet",
                    description = "Everything about your Pets",
                    externalDocs = OpenApiExternalDocsDto(
                        description = "Find out more",
                        url = Uri.of("http://swagger.io")
                    )
                ),
                OpenApiTagDto(
                    name = "store",
                    description = "Access to Petstore orders",
                    externalDocs = OpenApiExternalDocsDto(
                        description = "Find out more about our store",
                        url = Uri.of("http://swagger.io")
                    )
                ),
                OpenApiTagDto(
                    name = "user",
                    description = "Operations about user",
                )
            ),
            paths = mapOf(
                "/pet" to OpenApiPathItemDto(
                    put = OpenApiOperationDto(
                        tags =  listOf("pet"),
                        summary = "Update an existing pet",
                        description = "Update an existing pet by Id",
                        operationId = "updatePet",
                        requestBody = OpenApiRequestBodyDto(
                            description = "Update an existent pet in the store",
                            content = mapOf(
                                "application/json" to OpenApiMediaTypeDto(
                                    schema = OpenApiSchemaDto(
                                        `$ref` = "#/components/schemas/Pet",
                                    )
                                ),
                                "application/xml" to OpenApiMediaTypeDto(
                                    schema = OpenApiSchemaDto(
                                        `$ref` = "#/components/schemas/Pet",
                                    )
                                ),
                                "application/x-www-form-urlencoded" to OpenApiMediaTypeDto(
                                    schema = OpenApiSchemaDto(
                                        `$ref` = "#/components/schemas/Pet",
                                    )
                                )
                            ),
                            required = true
                        ),
                        responses = mapOf(
                            "200" to OpenApiResponseDto(
                                description = "Successful operation",
                                content = mapOf(
                                    "application/json" to OpenApiMediaTypeDto(
                                        schema = OpenApiSchemaDto(
                                            `$ref` = "#/components/schemas/Pet",
                                        )
                                    ),
                                    "application/xml" to OpenApiMediaTypeDto(
                                        schema = OpenApiSchemaDto(
                                            `$ref` = "#/components/schemas/Pet",
                                        )
                                    )
                                )
                            ),
                            "400" to OpenApiResponseDto(
                                description = "Invalid ID supplied"
                            ),
                            "404" to OpenApiResponseDto(
                                description = "Pet not found"
                            ),
                            "405" to OpenApiResponseDto(
                                description = "Validation exception"
                            ),
                        ),
                        security = listOf(
                            mapOf(
                                "petstore_auth" to listOf("write:pets", "read:pets")
                            )
                        )
                    ),
                    post = OpenApiOperationDto(
                        tags = listOf("pet"),
                        summary = "Add a new pet to the store",
                        description = "Add a new pet to the store",
                        operationId = "addPet",
                        requestBody = OpenApiRequestBodyDto(
                            description = "Create a new pet in the store",
                            content = mapOf(
                                "application/json" to OpenApiMediaTypeDto(
                                    schema = OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet")
                                ),
                                "application/xml" to OpenApiMediaTypeDto(
                                    schema = OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet")
                                ),
                                "application/x-www-form-urlencoded" to OpenApiMediaTypeDto(
                                    schema = OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet")
                                )
                            ),
                            required = true
                        ),
                        responses = mapOf(
                            "200" to OpenApiResponseDto(
                                description = "Successful operation",
                                content = mapOf(
                                    "application/json" to OpenApiMediaTypeDto(
                                        schema = OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet")
                                    ),
                                    "application/xml" to OpenApiMediaTypeDto(
                                        schema = OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet")
                                    ),
                                )
                            ),
                            "405" to OpenApiResponseDto("Invalid input")
                        ),
                        security = listOf(
                            mapOf(
                                "petstore_auth" to listOf("write:pets", "read:pets")
                            )
                        )
                    )
                ),
                "/pet/findByStatus" to OpenApiPathItemDto(
                    get = OpenApiOperationDto(
                        tags = listOf("pet"),
                        summary = "Finds Pets by status",
                        description = "Multiple status values can be provided with comma separated strings",
                        operationId = "findPetsByStatus",
                        parameters = listOf(
                            OpenApiParameterDto(
                                name = "status",
                                `in` = OpenApiParameterDto.Location.query,
                                description = "Status values that need to be considered for filter",
                                required = false,
                                explode = true,
                                schema = OpenApiSchemaDto(
                                    type = "string",
                                    default = "available",
                                    enum = listOf("available", "pending", "sold")
                                )
                            ),
                        ),
                        responses = mapOf(
                            "200" to OpenApiResponseDto(
                                description = "successful operation",
                                content = mapOf(
                                    "application/json" to OpenApiMediaTypeDto(
                                        schema = OpenApiSchemaDto(
                                            type = "array",
                                            items = OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet")
                                        )
                                    ),
                                    "application/xml" to OpenApiMediaTypeDto(
                                        schema = OpenApiSchemaDto(
                                            type = "array",
                                            items = OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet")
                                        )
                                    )
                                )
                            ),
                            "400" to OpenApiResponseDto(
                                description = "Invalid status value"
                            )
                        ),
                        security = listOf(
                            mapOf(
                                "petstore_auth" to listOf("write:pets", "read:pets")
                            )
                        )
                    )
                ),
                "/pet/findByTags" to OpenApiPathItemDto(
                    get = OpenApiOperationDto(
                        tags = listOf("pet"),
                        summary = "Finds Pets by tags",
                        description = "Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing.",
                        operationId = "findPetsByTags",
                        parameters = listOf(
                            OpenApiParameterDto(
                                name = "tags",
                                description = "Tags to filter by",
                                `in` = OpenApiParameterDto.Location.query,
                                required = false,
                                explode = true,
                                schema = OpenApiSchemaDto(
                                    type = "array",
                                    items = OpenApiSchemaDto("string")
                                )
                            )
                        ),
                        responses = mapOf(
                            "200" to OpenApiResponseDto(
                                description = "successful operation",
                                content = mapOf(
                                    "application/json" to OpenApiMediaTypeDto(
                                        schema = OpenApiSchemaDto(
                                            type = "array",
                                            items = OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet")
                                        )
                                    ),
                                    "application/xml" to OpenApiMediaTypeDto(
                                        schema = OpenApiSchemaDto(
                                            type = "array",
                                            items = OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet")
                                        )
                                    )
                                )
                            ),
                            "400" to OpenApiResponseDto(
                                description = "Invalid tag value"
                            )
                        ),
                        security = listOf(
                            mapOf(
                                "petstore_auth" to listOf("write:pets", "read:pets")
                            )
                        )
                    )
                ),
                "/pet/{petid}" to OpenApiPathItemDto(
                    get = OpenApiOperationDto(
                        tags = listOf("pet"),
                        summary = "Find pet by ID",
                        description = "Returns a single pet",
                        operationId = "getPetById",
                        parameters = listOf(
                            OpenApiParameterDto(
                                name = "petId",
                                `in` = OpenApiParameterDto.Location.path,
                                description = "ID of pet to return",
                                required = true,
                                schema = OpenApiSchemaDto("integer", "int64")
                            )
                        ),
                        responses = mapOf(
                            "200" to OpenApiResponseDto(
                                description = "successful operation",
                                content = mapOf(
                                    "application/json" to OpenApiMediaTypeDto(
                                        OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet")
                                    ),
                                    "application/xml" to OpenApiMediaTypeDto(
                                        OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet")
                                    )
                                )
                            ),
                            "400" to OpenApiResponseDto(
                                description = "Invalid ID supplied"
                            ),
                            "404" to OpenApiResponseDto(
                                description = "Pet not found"
                            )
                        ),
                        security = listOf(
                            mapOf(
                                "api_key" to emptyList(),
                            ),
                            mapOf(
                                "petstore_auth" to listOf("write:pets", "read:pets")
                            )
                        )
                    ),
                    post = OpenApiOperationDto(
                        tags = listOf("pet"),
                        summary = "Updates a pet in the store with form data",
                        description = "",
                        operationId = "updatePetWithForm",
                        parameters = listOf(
                            OpenApiParameterDto(
                                name = "petId",
                                `in` = OpenApiParameterDto.Location.path,
                                description = "ID of pet that needs to be updated",
                                required = true,
                                schema = OpenApiSchemaDto("integer", "int64")
                            ),
                            OpenApiParameterDto(
                                name = "name",
                                `in` = OpenApiParameterDto.Location.query,
                                description = "Name of pet that needs to be updated",
                                schema = OpenApiSchemaDto("string")
                            ),
                            OpenApiParameterDto(
                                name = "status",
                                `in` = OpenApiParameterDto.Location.query,
                                description = "Status of pet that needs to be updated",
                                schema = OpenApiSchemaDto("string")
                            )
                        ),
                        responses = mapOf(
                            "405" to OpenApiResponseDto(
                                description = "Invalid input"
                            )
                        ),
                        security = listOf(
                            mapOf(
                                "petstore_auth" to listOf("write:pets", "read:pets")
                            )
                        )
                    ),
                    delete = OpenApiOperationDto(
                        tags = listOf("pet"),
                        summary = "Deletes a pet",
                        description = "delete a pet",
                        operationId = "deletePet",
                        parameters = listOf(
                            OpenApiParameterDto(
                                name = "api_key",
                                `in` = OpenApiParameterDto.Location.header,
                                description = "",
                                required = false,
                                schema = OpenApiSchemaDto("string")
                            ),
                            OpenApiParameterDto(
                                name = "petId",
                                `in` = OpenApiParameterDto.Location.path,
                                description = "Pet id to delete",
                                required = true,
                                schema = OpenApiSchemaDto("integer", "int64")
                            )
                        ),
                        responses = mapOf(
                            "400" to OpenApiResponseDto(
                                description = "Invalid pet value"
                            )
                        ),
                        security = listOf(
                            mapOf(
                                "petstore_auth" to listOf("write:pets", "read:pets")
                            )
                        )
                    )
                ),
                "/pet/{petId}/uploadImage" to OpenApiPathItemDto(
                    post = OpenApiOperationDto(
                        tags = listOf("pet"),
                        summary = "uploads an image",
                        description = "",
                        operationId = "uploadFile",
                        parameters = listOf(
                            OpenApiParameterDto(
                                name = "petId",
                                `in` = OpenApiParameterDto.Location.path,
                                description = "ID of pet to update",
                                required = true,
                                schema = OpenApiSchemaDto("integer", "int64")
                            ),
                            OpenApiParameterDto(
                                name = "additionalMetadata",
                                `in` = OpenApiParameterDto.Location.query,
                                description = "Additional Metadata",
                                required = false,
                                schema = OpenApiSchemaDto("string")
                            )
                        ),
                        requestBody = OpenApiRequestBodyDto(
                            content = mapOf(
                                "application/octet-stream" to OpenApiMediaTypeDto(
                                    schema = OpenApiSchemaDto("string", "binary")
                                )
                            )
                        ),
                        responses = mapOf(
                            "200" to OpenApiResponseDto(
                                description = "successful operation",
                                content = mapOf(
                                    "application/json" to OpenApiMediaTypeDto(
                                        OpenApiSchemaDto(`$ref` = "#/components/schemas/ApiResponse")
                                    )
                                )
                            )
                        ),
                        security = listOf(
                            mapOf(
                                "petstore_auth" to listOf("write:pets", "read:pets")
                            )
                        )
                    )
                ),
                "store/inventory" to OpenApiPathItemDto(
                    get = OpenApiOperationDto(
                        tags = listOf("store"),
                        summary = "Returns pet inventories by status",
                        description = "Returns a map of status codes to quantities",
                        operationId = "getInventory",
                        responses = mapOf(
                            "200" to OpenApiResponseDto(
                                description = "successful operation",
                                content = mapOf(
                                    "application/json" to OpenApiMediaTypeDto(
                                        schema = OpenApiSchemaDto(
                                            type = "object",
                                            additionalProperties = OpenApiSchemaDto("integer", "int32")
                                        )
                                    )
                                )
                            )
                        ),
                        security = listOf(
                            mapOf(
                                "api_key" to emptyList()
                            )
                        )
                    )
                ),
                "store/order" to OpenApiPathItemDto(
                    post = OpenApiOperationDto(
                        tags = listOf("store"),
                        summary = "Place an order for a pet",
                        description = "Place a new order in the store",
                        operationId = "placeOrder",
                        requestBody = OpenApiRequestBodyDto(
                            content = mapOf(
                                "application/json" to OpenApiMediaTypeDto(
                                    OpenApiSchemaDto(`$ref` = "#/components/schemas/Order")
                                ),
                                "application/xml" to OpenApiMediaTypeDto(
                                    OpenApiSchemaDto(`$ref` = "#/components/schemas/Order")
                                ),
                                "application/x-www-form-urlencoded" to OpenApiMediaTypeDto(
                                    OpenApiSchemaDto(`$ref` = "#/components/schemas/Order")
                                )
                            )
                        ),
                        responses = mapOf(
                            "200" to OpenApiResponseDto(
                                description = "successful operation",
                                content = mapOf(
                                    "application/json" to OpenApiMediaTypeDto(
                                        OpenApiSchemaDto(`$ref` = "#/components/schemas/Order")
                                    )
                                )
                            ),
                            "405" to OpenApiResponseDto(
                                description = "Invalid input"
                            )
                        )
                    )
                ),
                "/store/order/{orderId}" to OpenApiPathItemDto(
                    get = OpenApiOperationDto(
                        tags = listOf("store"),
                        summary = "Find purchase order by ID",
                        description = "For valid response try integer IDs with value <= 5 or > 10. Other values will generate exceptions.",
                        operationId = "getOrderById",
                        parameters = listOf(
                            OpenApiParameterDto(
                                name = "orderId",
                                `in` = OpenApiParameterDto.Location.path,
                                description = "ID of order that needs to be fetched",
                                required = true,
                                schema = OpenApiSchemaDto("integer", "int64")
                            )
                        ),
                        responses = mapOf(
                            "200" to OpenApiResponseDto(
                                description = "successful operation",
                                content = mapOf(
                                    "application/json" to OpenApiMediaTypeDto(
                                        OpenApiSchemaDto(`$ref` = "#/components/schemas/Order")
                                    ),
                                    "application/xml" to OpenApiMediaTypeDto(
                                        OpenApiSchemaDto(`$ref` = "#/components/schemas/Order")
                                    )
                                )
                            ),
                            "400" to OpenApiResponseDto(
                                description = "Invalid ID supplied"
                            ),
                            "404" to OpenApiResponseDto(
                                description = "Order not found"
                            )
                        )
                    ),
                    delete = OpenApiOperationDto(
                        tags = listOf("store"),
                        summary = "Delete purchase order by ID",
                        description = "For valid response try integer IDs with value < 1000. Anything above 1000 or nonintegers will generate API errors",
                        operationId = "deleteOrder",
                        parameters = listOf(
                            OpenApiParameterDto(
                                name = "orderId",
                                `in` = OpenApiParameterDto.Location.path,
                                description = "ID of the order that needs to be deleted",
                                required = true,
                                schema = OpenApiSchemaDto("integer", "int64")
                            )
                        ),
                        responses = mapOf(
                            "400" to OpenApiResponseDto("Invalid ID supplied"),
                            "404" to OpenApiResponseDto("Order not found")
                        )
                    )
                ),
                "/user" to OpenApiPathItemDto(
                    post = OpenApiOperationDto(
                        tags = listOf("user"),
                        summary = "Create user",
                        description = "This can only be done by the logged in user.",
                        operationId = "createUser",
                        requestBody = OpenApiRequestBodyDto(
                            description = "Created user object",
                            content = mapOf(
                                "application/json" to OpenApiMediaTypeDto(
                                    OpenApiSchemaDto(`$ref` = "#/components/schemas/User")
                                ),
                                "application/xml" to OpenApiMediaTypeDto(
                                    OpenApiSchemaDto(`$ref` = "#/components/schemas/User")
                                ),
                                "application/x-www-form-urlencoded" to OpenApiMediaTypeDto(
                                    OpenApiSchemaDto(`$ref` = "#/components/schemas/User")
                                )
                            )
                        ),
                        responses = mapOf(
                            "default" to OpenApiResponseDto(
                                description = "successful operation",
                                content = mapOf(
                                    "application/json" to OpenApiMediaTypeDto(
                                        OpenApiSchemaDto(`$ref` = "#/components/schemas/User")
                                    ),
                                    "application/xml" to OpenApiMediaTypeDto(
                                        OpenApiSchemaDto(`$ref` = "#/components/schemas/User")
                                    ),
                                )
                            )
                        )
                    )
                ),
                "/user/createWithList" to OpenApiPathItemDto(
                    post = OpenApiOperationDto(
                        tags = listOf("user"),
                        summary = "Creates list of users with given input array",
                        description = "Creates list of users with given input array",
                        operationId = "createUsersWithListInput",
                            requestBody = OpenApiRequestBodyDto(
                                content = mapOf(
                                    "application/json" to OpenApiMediaTypeDto(
                                        OpenApiSchemaDto(
                                            type = "array",
                                            items = OpenApiSchemaDto(`$ref` = "#/components/schemas/User")
                                        )
                                    )
                                )
                            ),
                            responses = mapOf(
                                "200" to OpenApiResponseDto(
                                    description = "Successful operation",
                                    content = mapOf(
                                        "application/json" to OpenApiMediaTypeDto(OpenApiSchemaDto(`$ref` = "#/components/schemas/User")),
                                        "application/xml" to OpenApiMediaTypeDto(OpenApiSchemaDto(`$ref` = "#/components/schemas/User"))
                                    )
                                ),
                                "default" to OpenApiResponseDto("successful operation")
                            )
                    )
                ),
                "/user/login" to OpenApiPathItemDto(
                    get = OpenApiOperationDto(
                        tags = listOf("user"),
                        summary = "Logs user into the system",
                        description = "",
                        operationId = "loginUser",
                        parameters = listOf(
                            OpenApiParameterDto(
                                name = "username",
                                `in` = OpenApiParameterDto.Location.query,
                                description = "The user name for login",
                                required = false,
                                schema = OpenApiSchemaDto("string")
                            ),
                            OpenApiParameterDto(
                                name = "password",
                                `in` = OpenApiParameterDto.Location.query,
                                description = "The password for login in clear text",
                                required = false,
                                schema = OpenApiSchemaDto("string")
                            )
                        ),
                        responses = mapOf(
                            "200" to OpenApiResponseDto(
                                description = "successful operation",
                                headers = mapOf(
                                    "X-Rate-Limit" to OpenApiHeaderDto(
                                        description = "calls per hour allowed by the user",
                                        schema = OpenApiSchemaDto("integer", "int32")
                                    ),
                                    "X-Expires-After" to OpenApiHeaderDto(
                                        description = "date in UTC when token expires",
                                        schema = OpenApiSchemaDto("string", "date-time")
                                    )
                                ),
                                content = mapOf(
                                    "application/xml" to OpenApiMediaTypeDto(OpenApiSchemaDto("string")),
                                    "application/json" to OpenApiMediaTypeDto(OpenApiSchemaDto("string"))
                                )
                            ),
                            "400" to OpenApiResponseDto("Invalid username/password supplied")
                        )
                    )
                ),
                "/user/login" to OpenApiPathItemDto(
                    get = OpenApiOperationDto(
                        tags = listOf("user"),
                        summary = "Logs out current logged in user session",
                        description = "",
                        operationId = "logoutUser",
                        parameters = emptyList(),
                        responses = mapOf(
                            "default" to OpenApiResponseDto("successful operation")
                        )
                    )
                ),
                "/user/{username}" to OpenApiPathItemDto(
                    get = OpenApiOperationDto(
                        tags = listOf("user"),
                        summary = "Get user by user name",
                        description = "",
                        operationId = "getUserByName",
                        parameters = listOf(
                            OpenApiParameterDto(
                                name = "username",
                                `in` = OpenApiParameterDto.Location.path,
                                description = "The name that needs to be fetched. Use user1 for testing. ",
                                required = true,
                                schema = OpenApiSchemaDto("string")
                            )
                        ),
                        responses = mapOf(
                            "200" to OpenApiResponseDto(
                                description = "successful operation",
                                content = mapOf(
                                    "application/json" to OpenApiMediaTypeDto(OpenApiSchemaDto(`$ref` = "#/components/schemas/User")),
                                    "application/xml" to OpenApiMediaTypeDto(OpenApiSchemaDto(`$ref` = "#/components/schemas/User"))
                                )
                            ),
                            "400" to OpenApiResponseDto("Invalid username supplied"),
                            "404" to OpenApiResponseDto("User not found")
                        )
                    ),
                    put = OpenApiOperationDto(
                        tags = listOf("user"),
                        summary = "Update user",
                        description = "This can only be done by the logged in user.",
                        operationId = "updateUser",
                        parameters = listOf(
                            OpenApiParameterDto(
                                name = "username",
                                `in` = OpenApiParameterDto.Location.path,
                                description = "name that need to be deleted",
                                required = true,
                                schema = OpenApiSchemaDto("string")
                            )
                        ),
                        requestBody = OpenApiRequestBodyDto(
                            description = "Update an existent user in the store",
                            content = mapOf(
                                "application/json" to OpenApiMediaTypeDto(OpenApiSchemaDto(`$ref` = "#/components/schemas/User")),
                                "application/xml" to OpenApiMediaTypeDto(OpenApiSchemaDto(`$ref` = "#/components/schemas/User")),
                                "application/x-www-form-urlencoded" to OpenApiMediaTypeDto(OpenApiSchemaDto(`$ref` = "#/components/schemas/User"))
                            )
                        ),
                        responses = mapOf(
                            "default" to OpenApiResponseDto("successful operation")
                        )
                    ),
                    delete = OpenApiOperationDto(
                        tags = listOf("user"),
                        summary = "Delete user",
                        description = "This can only be done by the logged in user.",
                        operationId = "deleteUser",
                        parameters = listOf(
                            OpenApiParameterDto(
                                name = "username",
                                `in` = OpenApiParameterDto.Location.path,
                                description = "The name that needs to be deleted",
                                required = true,
                                schema = OpenApiSchemaDto("string")
                            )
                        ),
                        responses = mapOf(
                            "400" to OpenApiResponseDto("Invalid username supplied"),
                            "404" to OpenApiResponseDto("User not found")
                        )
                    )
                )
            ),
            components = OpenApiComponentsDto(
                schemas = mapOf(
                    "Order" to OpenApiSchemaDto(
                        type = "object",
                        properties = mapOf(
                            "id" to OpenApiSchemaDto("integer", "int64", example = 10),
                            "petId" to OpenApiSchemaDto("integer", "int64", example = 198772),
                            "quantity" to OpenApiSchemaDto("integer", "int32", example = 7),
                            "shipDate" to OpenApiSchemaDto("string", "date-time"),
                            "status" to OpenApiSchemaDto(
                                type = "string",
                                description = "Order Status",
                                enum = listOf("placed", "approved", "delivered"),
                                example = "approved"
                            ),
                            "complete" to OpenApiSchemaDto("boolean")
                        ),
                        xml = OpenApiSchemaDto.Xml("order")
                    ),
                    "Customer" to OpenApiSchemaDto(
                        type = "object",
                        properties = mapOf(
                            "id" to OpenApiSchemaDto("integer", "int64", example = 100000),
                            "username" to OpenApiSchemaDto("string", example = "fehguy"),
                            "address" to OpenApiSchemaDto(
                                type = "array",
                                xml = OpenApiSchemaDto.Xml("addresses", wrapped = true),
                                items = OpenApiSchemaDto(`$ref` = "#/components/schemas/Address")
                            )
                        ),
                        xml = OpenApiSchemaDto.Xml("customer")
                    ),
                    "Address" to OpenApiSchemaDto(
                        type = "object",
                        properties = mapOf(
                            "street" to OpenApiSchemaDto("string", example = "437 Lytton"),
                            "city" to OpenApiSchemaDto("string", example = "Palo Alto"),
                            "state" to OpenApiSchemaDto("string", example = "CA"),
                            "zip" to OpenApiSchemaDto("string", example = "94301")
                        ),
                        xml = OpenApiSchemaDto.Xml("address")
                    ),
                    "Category" to OpenApiSchemaDto(
                        type = "object",
                        properties = mapOf(
                            "id" to OpenApiSchemaDto("integer", "int64", example = 1),
                            "name" to OpenApiSchemaDto("string", example = "Dogs")
                        ),
                        xml = OpenApiSchemaDto.Xml("category")
                    ),
                    "User" to OpenApiSchemaDto(
                        type = "object",
                        properties = mapOf(
                            "id" to OpenApiSchemaDto("integer", "int64", example = 10),
                            "username" to OpenApiSchemaDto("string", example = "theUser"),
                            "firstName" to OpenApiSchemaDto("string", example = "John"),
                            "lastName" to OpenApiSchemaDto("string", example = "James"),
                            "email" to OpenApiSchemaDto("string", example = "john@email.com"),
                            "password" to OpenApiSchemaDto("string", example = "12345"),
                            "phone" to OpenApiSchemaDto("string", example = "12345"),
                            "userStatus" to OpenApiSchemaDto("integer", "int32", "User Status", example = 1)
                        ),
                        xml = OpenApiSchemaDto.Xml("user")
                    ),
                    "Tag" to OpenApiSchemaDto(
                        type = "object",
                        properties = mapOf(
                            "id" to OpenApiSchemaDto("integer", "int64"),
                            "name" to OpenApiSchemaDto("string")
                        ),
                        xml = OpenApiSchemaDto.Xml("tag")
                    ),
                    "Pet" to OpenApiSchemaDto(
                        required = listOf("name", "photoUrls"),
                        type = "object",
                        properties = mapOf(
                            "id" to OpenApiSchemaDto("integer", "int64", example = 10),
                            "name" to OpenApiSchemaDto("string", example = "doggie"),
                            "category" to OpenApiSchemaDto(`$ref` = "#/components/schemas/Category"),
                            "photoUrls" to OpenApiSchemaDto(
                                type = "array",
                                xml = OpenApiSchemaDto.Xml(wrapped = true),
                                items = OpenApiSchemaDto(
                                    type = "string",
                                    xml = OpenApiSchemaDto.Xml("photoUrl")
                                )
                            ),
                            "status" to OpenApiSchemaDto(
                                type = "string",
                                description = "pet status in the store",
                                enum = listOf("available", "pending", "sold")
                            ),
                            "tags" to OpenApiSchemaDto(
                                type = "array",
                                items = OpenApiSchemaDto(`$ref` = "#/components/schemas/Tag"),
                                xml = OpenApiSchemaDto.Xml(wrapped = true)
                            )
                        ),
                        xml = OpenApiSchemaDto.Xml("pet")
                    ),
                    "ApiResponse" to OpenApiSchemaDto(
                        type = "object",
                        properties = mapOf(
                            "code" to OpenApiSchemaDto("integer", "int32"),
                            "type" to OpenApiSchemaDto("string"),
                            "message" to OpenApiSchemaDto("string")
                        ),
                        xml = OpenApiSchemaDto.Xml("##default")
                    )
                ),
                requestBodies = mapOf(
                    "Pet" to OpenApiRequestBodyDto(
                        description = "Pet object that needs to be added to the store",
                        content = mapOf(
                            "application/json" to OpenApiMediaTypeDto(OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet")),
                            "application/xml" to OpenApiMediaTypeDto(OpenApiSchemaDto(`$ref` = "#/components/schemas/Pet"))
                        )
                    ),
                    "UserArray" to OpenApiRequestBodyDto(
                        description = "List of user object",
                        content = mapOf(
                            "application/json" to OpenApiMediaTypeDto(
                                OpenApiSchemaDto("array", items = OpenApiSchemaDto(`$ref` = "#/components/schemas/User"))
                            )
                        )
                    )
                ),
                securitySchemes = mapOf(
                    "petstore_auth" to OpenApiSecuritySchemeDto(
                        type = OpenApiSecuritySchemeDto.Type.oauth2,
                        flows = OpenApiSecuritySchemeDto.Flows(
                            implicit = OpenApiSecuritySchemeDto.Flow(
                                authorizationUrl = Uri.of("https://petstore3.swagger.io/oauth/authorize"),
                                scopes = mapOf(
                                    "write:pets" to "modify pets in your account",
                                    "read:pets" to "read your pets"
                                )
                            )
                        )
                    ),
                    "api_key" to OpenApiSecuritySchemeDto(
                        type = OpenApiSecuritySchemeDto.Type.apiKey,
                        name = "api_key",
                        `in` = OpenApiSecuritySchemeDto.Location.header
                    )
                )
            )
        )

        approval.assertApproved(MoshiYaml.asFormatString(petStore), contentType = ContentType.APPLICATION_YAML)
    }
}
