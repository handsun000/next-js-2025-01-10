import createClient from "openapi-fetch";
import type { paths } from "@/src/lib/backend/apiV1/schema";
import Link from "next/link";

const client = createClient<paths>({
    baseUrl: "http://localhost:8080",
})

export default async function Page({
    searchParams,
}: {
    searchParams: {
        searchKeywordType?: "title" | "content";
        searchKeyword?: string;
        pageSize?: number;
        page?: number;
    }
}) {

    const { searchKeyword = "", searchKeywordType = "title", pageSize = 10, page = 1 } = await searchParams;
    const respone = await client.GET("/api/v1/posts", {
        params: {
            query: {
                searchKeyword,
                searchKeywordType,
                pageSize,
                page,
            }
        }
    });

    const responeBody = respone.data!!;

    return <div>
        <form>
            <input type="hidden" name="page" value="1" />
            <select name="pageSize" defaultValue={pageSize}>
                <option disabled>페이지당 행 수</option>
                <option value="10">10</option>
                <option value="30">30</option>
                <option value="50">50</option>
            </select>
            <select name="searchKeywordType" defaultValue={searchKeywordType}>
                <option disabled>검색어 타입</option>
                <option value="title">제목</option>
                <option value="content">내용</option>
            </select>
            <input type="text" name="searchKeyword" defaultValue={searchKeyword} />
            <button type="submit">검색</button>
        </form>

        <div>
            currentPageNumber: {responeBody.currentPageNumber}
        </div>

        <div>
            pageSize: {responeBody.pageSize}
        </div>

        <div>
            totalPages: {responeBody.totalPages}
        </div>

        <div>totalItems: {responeBody.totalItems}</div>

        <hr />

        <ul>
            {responeBody.items.map((item) => (
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

        <div>
            {Array.from({ length: responeBody.totalPages }, (_, i) => i + 1).map(
                (pageNum) => (
                    <Link
                        key={pageNum}
                        className="mx-1 px-2 py-1 border rounded"
                        href={`?page=${pageNum}&pageSize=${pageSize}&searchKeywordType=${searchKeywordType}&searchKeyword=${searchKeyword}`}
                    >
                        {pageNum}
                    </Link>
                )
            )}
        </div>
    </div>;
}