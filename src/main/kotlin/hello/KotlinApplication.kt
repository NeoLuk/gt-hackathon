package hello

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import kotlin.math.abs

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

                val highestPlayer = arenaUpdate.arena.state.toList().maxByOrNull {
                    it.second.score
                }!!
                if (selfId == highestPlayer.first) {
                    println("isHighest")
                    Action.Attack.action
                } else {
                    println("findTarget")
                    findHighestPlayer(highestPlayer.second, mySelf)
                }
            }
        }
    }

    fun findHighestPlayer(target: PlayerState, mySelf: PlayerState): Mono<ServerResponse> {
        val diffX = target.x - mySelf.x
        val diffY = target.y - mySelf.y
        val direction = mySelf.direction

        println("diffX = $diffX , diffY = $diffY")
        if (diffX == 0) {
            if (diffY > 2) {
                return when (direction) {
                    "S" -> Action.Move.action
                    "E" -> Action.Right.action
                    else -> Action.Left.action
                }
            } else if (diffY < -2) {
                return when (direction) {
                    "N" -> Action.Move.action
                    "E" -> Action.Left.action
                    else -> Action.Right.action

                }
            }
            if (diffY in 1..2) {
                return when (direction) {
                    "S" -> Action.Attack.action
                    "E" -> Action.Right.action
                    else -> Action.Left.action

                }
            } else if (diffY in -2..-1) {
                return when (direction) {

                    "N" -> Action.Attack.action
                    "E" -> Action.Left.action
                    else -> Action.Right.action
                }
            }
        } else if (diffY == 0) {
            if (diffX > 2) {
                return when (direction) {
                    "E" -> Action.Move.action
                    "N" -> Action.Right.action
                    else -> Action.Left.action
                }
            } else if (diffX < -2) {
                return when (direction) {
                    "W" -> Action.Move.action
                    "N" -> Action.Left.action
                    else -> Action.Right.action

                }
            }
            if (diffX in 1..2) {
                return when (direction) {
                    "E" -> Action.Attack.action
                    "N" -> Action.Right.action
                    else -> Action.Left.action
                }
            } else if (diffX in -2..-1) {
                return when (direction) {
                    "W" -> Action.Attack.action
                    "N" -> Action.Left.action
                    else -> Action.Right.action


                }
            }
        } else if (abs(diffY) > abs(diffX)) {
            if (diffY > 1) {
                return when (direction) {
                    "S" -> Action.Move.action
                    "E" -> Action.Right.action
                    else -> Action.Left.action

                }
            } else if (diffY < -1) {
                return when (direction) {
                    "N" -> Action.Move.action
                    "E" -> Action.Left.action
                    else -> Action.Right.action
                }
            } else {
                return Action.Move.action
            }
        } else if (abs(diffX) > abs(diffY)) {
            if (diffX > 1) {
                return when (direction) {
                    "E" -> Action.Move.action
                    "N" -> Action.Right.action
                    else -> Action.Left.action
                }
            } else if (diffX < -1) {
                return when (direction) {
                    "W" -> Action.Move.action
                    "N" -> Action.Left.action
                    else -> Action.Right.action

                }
            } else {
                return Action.Move.action
            }
        } else {
            return Action.Move.action
        }
        println("attack?")
        return Action.Attack.action
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
