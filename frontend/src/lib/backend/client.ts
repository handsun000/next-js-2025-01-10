import createClient from "openapi-fetch";
import { paths } from "./apiV1/schema";

const client = createClient<paths>({
    baseUrl: "http://localhost:8080",
});

export default client;