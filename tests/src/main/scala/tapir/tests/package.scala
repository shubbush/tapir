package tapir

import java.io.InputStream
import java.nio.ByteBuffer

import io.circe.generic.auto._
import tapir.json.circe._
import com.softwaremill.macwire._

package object tests {

  val in_query_out_string: Endpoint[String, Unit, String] = endpoint.in(query[String]("fruit")).out(stringBody)

  val in_query_query_out_string: Endpoint[(String, Option[Int]), Unit, String] =
    endpoint.in(query[String]("fruit")).in(query[Option[Int]]("amount")).out(stringBody)

  val in_header_out_string: Endpoint[String, Unit, String] = endpoint.in(header[String]("X-Role")).out(stringBody)

  val in_path_path_out_string: Endpoint[(String, Int), Unit, String] =
    endpoint.in("fruit" / path[String] / "amount" / path[Int]).out(stringBody)

  val in_string_out_string: Endpoint[String, Unit, String] = endpoint.post.in("api" / "echo").in(stringBody).out(stringBody)

  val in_mapped_query_out_string: Endpoint[List[Char], Unit, String] =
    endpoint.in(query[String]("fruit").map(_.toList)(_.mkString(""))).out(stringBody)

  val in_mapped_path_out_string: Endpoint[Fruit, Unit, String] =
    endpoint.in(("fruit" / path[String]).mapTo(Fruit)).out(stringBody)

  val in_mapped_path_path_out_string: Endpoint[FruitAmount, Unit, String] =
    endpoint.in(("fruit" / path[String] / "amount" / path[Int]).mapTo(FruitAmount)).out(stringBody)

  val in_query_mapped_path_path_out_string: Endpoint[(FruitAmount, String), Unit, String] = endpoint
    .in(("fruit" / path[String] / "amount" / path[Int]).mapTo(FruitAmount))
    .in(query[String]("color"))
    .out(stringBody)

  val in_query_out_mapped_string: Endpoint[String, Unit, List[Char]] =
    endpoint.in(query[String]("fruit")).out(stringBody.map(_.toList)(_.mkString("")))

  val in_query_out_mapped_string_header: Endpoint[String, Unit, FruitAmount] = endpoint
    .in(query[String]("fruit"))
    .out(stringBody.and(header[Int]("X-Role")).mapTo(FruitAmount))

  val in_json_out_json: Endpoint[FruitAmount, Unit, FruitAmount] =
    endpoint.post.in("api" / "echo").in(jsonBody[FruitAmount]).out(jsonBody[FruitAmount]).name("echo json")

  val in_byte_array_out_byte_array: Endpoint[Array[Byte], Unit, Array[Byte]] =
    endpoint.post.in("api" / "echo").in(byteArrayBody).out(byteArrayBody).name("echo byte array")

  val in_byte_buffer_out_byte_buffer: Endpoint[ByteBuffer, Unit, ByteBuffer] =
    endpoint.post.in("api" / "echo").in(byteBufferBody).out(byteBufferBody).name("echo byte buffer")

  val in_input_stream_out_input_stream: Endpoint[InputStream, Unit, InputStream] =
    endpoint.post.in("api" / "echo").in(inputStreamBody).out(inputStreamBody).name("echo input stream")

  val endpoint_with_path: Endpoint[Unit, Unit, String] =
    endpoint.in("fruit" / "amount").out(stringBody)

  val endpoint_with_status_mapping_no_body: Endpoint[Unit, Unit, Unit] =
    endpoint
      .in("api")

  val endpoint_with_status_mapping: Endpoint[Unit, Unit, String] =
    endpoint
      .in("api")
      .out(stringBody)

  val endpoint_with_error_status_mapping: Endpoint[Unit, String, Unit] =
    endpoint
      .in("api")
      .errorOut(stringBody)

  val allTestEndpoints: Set[Endpoint[_, _, _]] = wireSet[Endpoint[_, _, _]]
}
