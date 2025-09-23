import createClient from "openapi-fetch";
import type { paths, components } from "@/src/lib/backend/apiV1/schema";

const client = createClient<paths>({
    baseUrl: "http://localhost:8080",
})

export default async function Page({
    searchParams,
}: {
    searchParams: {
        searchKeywordType?: "title" | "content";
        searchKeyword?: string;
    }
}) {

    const { searchKeyword = "", searchKeywordType = "title" } = await searchParams;
    const respone = await client.GET("/api/v1/posts", {
        params: {
            query: {
                searchKeyword: searchKeyword,
                searchKeywordType: searchKeywordType,
            }
        }
    });

    const responeBody = respone.data;

    return <div>
        <form>
            <select name="searchKeywordType" defaultValue={searchKeywordType}>
                <option value="title">제목</option>
                <option value="content">내용</option>
            </select>
            <input type="text" name="searchKeyword" defaultValue={searchKeyword} />
            <button type="submit">검색</button>
        </form>

        <div>
            currentPageNumber: {responeBody?.currentPageNumber}
        </div>

        <div>
            pageSize: {responeBody?.pageSize}
        </div>

        <div>
            totalPages: {responeBody?.totalPages}
        </div>

        <div>totalItems: {responeBody?.totalItems}</div>

        <hr />

        <ul>
            {responeBody?.items.map((item) => (
                <li key={item.id} className="border-[2px] border-[red] my-3">
                    <div>id: {item.id}</div>
                    <div>createDate : {item.createDate}</div>
                    <div>modifyDate : {item.modifyDate}</div>
                    <div>authorId : {item.authorId}</div>
                    <div>modiauthorNameyDate : {item.authorName}</div>
                    <div>title : {item.title}</div>
                    <div>published : {`${item.published}`}</div>
                    <div>listed : {`${item.listed}`}</div>
                </li>
            ))}
        </ul>
    </div>;
}