package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Scoreboard;

public class ScoreboardSlotArgument implements ArgumentType<Integer> {
   private static final Collection<String> EXAMPLES = Arrays.asList("sidebar", "foo.bar");
   public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((p_109203_) -> {
      return Component.translatable("argument.scoreboardDisplaySlot.invalid", p_109203_);
   });

   private ScoreboardSlotArgument() {
   }

   public static ScoreboardSlotArgument displaySlot() {
      return new ScoreboardSlotArgument();
   }

   public static int getDisplaySlot(CommandContext<CommandSourceStack> pContext, String pSlot) {
      return pContext.getArgument(pSlot, Integer.class);
   }

   public Integer parse(StringReader pReader) throws CommandSyntaxException {
      String s = pReader.readUnquotedString();
      int i = Scoreboard.m_83504_(s);
      if (i == -1) {
         throw ERROR_INVALID_VALUE.create(s);
      } else {
         return i;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      return SharedSuggestionProvider.suggest(Scoreboard.m_83494_(), pBuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}