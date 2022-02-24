package hello

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

@SpringBootApplication
class KotlinApplication {

    @Bean
    fun routes() = router {
        GET {
            ServerResponse.ok().body(Mono.just("Let the battle begin!"))
        }

        POST("/**", accept(APPLICATION_JSON)) { request ->
            request.bodyToMono(ArenaUpdate::class.java).flatMap { arenaUpdate ->
                val selfId = arenaUpdate._links.self.href
                val width = arenaUpdate.arena.dims.first()
                val height = arenaUpdate.arena.dims.last()

                val mySelf: PlayerState = arenaUpdate.arena.state[selfId]!!
                println(mySelf.direction)
//                for (state in arenaUpdate.arena.state.values) {
//
//                }
                Action.Attack.action
            }
        }
    }

}

fun main(args: Array<String>) {
    runApplication<KotlinApplication>(*args)
}

enum class Action(val action: Mono<ServerResponse>) {
    Left(action = ServerResponse.ok().body(Mono.just("L"))),
    Right(action = ServerResponse.ok().body(Mono.just("R"))),
    Move(action = ServerResponse.ok().body(Mono.just("F"))),
    Attack(action = ServerResponse.ok().body(Mono.just("T")))
}

data class ArenaUpdate(val _links: Links, val arena: Arena)
data class PlayerState(val x: Int, val y: Int, val direction: String, val score: Int, val wasHit: Boolean)
data class Links(val self: Self)
data class Self(val href: String)
data class Arena(val dims: List<Int>, val state: Map<String, PlayerState>)
