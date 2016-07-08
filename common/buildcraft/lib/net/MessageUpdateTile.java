package buildcraft.lib.net;

import java.io.IOException;

import com.google.common.base.Throwables;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.lib.LibProxy;
import buildcraft.lib.net.command.IPayloadReceiver;
import buildcraft.lib.net.command.IPayloadWriter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MessageUpdateTile implements IMessage {
    private BlockPos pos;
    private PacketBuffer payload;
    private String className;

    /** Used by forge to construct this upon receive. Do not use! */
    @Deprecated
    public MessageUpdateTile() {}

    public MessageUpdateTile(BlockPos pos, String className, IPayloadWriter writer) {
        this.pos = pos;
        this.className = className;
        payload = new PacketBuffer(Unpooled.buffer());
        writer.write(payload);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        this.pos = buffer.readBlockPos();
        this.className = buffer.readStringFromBuffer(100);
        int size = buffer.readUnsignedShort();
        payload = new PacketBuffer(buffer.readBytes(size));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeBlockPos(pos);
        buffer.writeString(className);
        int length = payload.readableBytes();
        buffer.writeShort(length);
        buffer.writeBytes(payload, 0, length);
    }

    public enum Handler implements IMessageHandler<MessageUpdateTile, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(final MessageUpdateTile message, MessageContext ctx) {
            EntityPlayer player = LibProxy.getProxy().getPlayerForContext(ctx);
            if (player == null || player.worldObj == null) return null;
            TileEntity tile = player.worldObj.getTileEntity(message.pos);
            if (tile instanceof IPayloadReceiver && tile.getClass().getName().equals(message.className)) {
                try {
                    return ((IPayloadReceiver) tile).receivePayload(ctx.side, message.payload);
                } catch (IOException io) {
                    throw Throwables.propagate(io);
                }
            }
            return null;
        }
    }
}
